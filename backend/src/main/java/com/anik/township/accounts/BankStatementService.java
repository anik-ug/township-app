package com.anik.township.accounts;

import com.anik.township.auth.AuthUser;
import com.anik.township.billing.BillingService;
import com.anik.township.config.DateProvider;
import com.anik.township.domain.*;
import com.anik.township.repository.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Pattern;
import jakarta.validation.constraints.Positive;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BankStatementService {
    private static final long MAX_BYTES = 1_048_576;
    private static final int MAX_ROWS = 5_000;
    private static final Pattern MONEY = Pattern.compile("\\d+(\\.\\d{1,2})?");
    private final BankUploadRepository uploadRepository;
    private final BankRowRepository rowRepository;
    private final BankRowAuditRepository auditRepository;
    private final FlatRepository flatRepository;
    private final BillRepository billRepository;
    private final AppUserRepository userRepository;
    private final BillingService billingService;
    private final DateProvider dateProvider;

    public BankStatementService(BankUploadRepository uploadRepository, BankRowRepository rowRepository,
            BankRowAuditRepository auditRepository, FlatRepository flatRepository, BillRepository billRepository,
            AppUserRepository userRepository, BillingService billingService, DateProvider dateProvider) {
        this.uploadRepository = uploadRepository;
        this.rowRepository = rowRepository;
        this.auditRepository = auditRepository;
        this.flatRepository = flatRepository;
        this.billRepository = billRepository;
        this.userRepository = userRepository;
        this.billingService = billingService;
        this.dateProvider = dateProvider;
    }

    @Transactional
    public UploadView upload(MultipartFile file, AuthUser actor) {
        if (file == null || file.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CSV file must be at most 1 MB");
        }
        List<String[]> records = read(file);
        if (records.size() - 1 > MAX_ROWS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CSV may contain at most 5,000 rows");
        }
        AppUser user = actor.getUser();
        BankUpload upload = new BankUpload();
        upload.setFileName(file.getOriginalFilename() == null ? "bank.csv" : file.getOriginalFilename());
        upload.setUploadedBy(user);
        upload.setUploadedAt(dateProvider.now());
        uploadRepository.save(upload);
        Set<Key> seen = new HashSet<>();
        for (int i = 1; i < records.size(); i++) {
            processRecord(upload, records.get(i), i, seen);
        }
        return getUpload(upload.getId());
    }

    private void processRecord(BankUpload upload, String[] values, int line, Set<Key> seen) {
        String dateText = value(values, 0), amountText = value(values, 1), reference = value(values, 2);
        String upi = value(values, 3), narration = value(values, 4);
        LocalDate date = dateProvider.asOfDate();
        long amount = 1;
        String error = null;
        try {
            date = LocalDate.parse(dateText);
        } catch (RuntimeException ex) {
            error = "Invalid ISO date";
        }
        try {
            if (!MONEY.matcher(amountText).matches()) throw new IllegalArgumentException();
            amount = new BigDecimal(amountText).movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
            if (amount <= 0) throw new IllegalArgumentException();
        } catch (RuntimeException ex) {
            error = error == null ? "Amount must be a positive rupee decimal with at most 2 decimals" : error + "; invalid amount";
        }
        if (reference.isBlank()) error = error == null ? "Reference is required" : error + "; reference is required";
        if (error != null) {
            saveRow(upload, date, amount, reference.isBlank() ? "LINE-" + line : reference, upi, narration,
                    BankRowStatus.ERROR, 0, error, null, null, seen);
            return;
        }
        BankStatementMatcher.Row row = new BankStatementMatcher.Row(date, amount, reference, upi, narration);
        BankStatementMatcher.Result result = new BankStatementMatcher().match(row, new Lookup(seen));
        if (result.status() == BankStatementMatcher.Status.DUPLICATE) {
            saveRow(upload, date, amount, reference, upi, narration, BankRowStatus.DUPLICATE,
                    result.confidence(), result.explanation(), null, null, seen);
            return;
        }
        saveRow(upload, date, amount, reference, upi, narration, BankRowStatus.valueOf(result.status().name()),
                result.confidence(), result.explanation(), result.flatId(), result.billId(), seen);
    }

    private void saveRow(BankUpload upload, LocalDate date, long amount, String reference, String upi,
            String narration, BankRowStatus status, int confidence, String explanation, Long flatId,
            Long billId, Set<Key> seen) {
        Key key = new Key(reference, amount, date);
        if (status == BankRowStatus.DUPLICATE || seen.contains(key)) {
            return;
        }
        if (rowRepository.findByReferenceAndAmountPaiseAndTxnDate(reference, amount, date).isPresent()) {
            seen.add(key);
            return;
        }
        BankRow row = new BankRow();
        row.setUpload(upload); row.setTxnDate(date); row.setAmountPaise(amount); row.setReference(reference);
        row.setUpiId(upi); row.setNarration(narration); row.setStatus(status); row.setConfidence(confidence);
        row.setExplanation(explanation); row.setMatchedFlat(flatId == null ? null : flatRepository.findById(flatId).orElse(null));
        row.setMatchedBill(billId == null ? null : billRepository.findById(billId).orElse(null));
        try {
            rowRepository.saveAndFlush(row);
            seen.add(key);
            if (status == BankRowStatus.MATCHED) {
                Bill bill = billRepository.findByIdForUpdate(billId).orElseThrow();
                billingService.createPayment(bill, amount, PaymentMethod.BANK_MATCH, reference, row);
            }
        } catch (DataIntegrityViolationException ex) {
            rowRepository.findByReferenceAndAmountPaiseAndTxnDate(reference, amount, date)
                    .ifPresent(existing -> seen.add(key));
        }
    }

    @Transactional(readOnly = true)
    public UploadView getUpload(long id) {
        BankUpload upload = uploadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Upload not found"));
        List<RowView> rows = rowRepository.findByUploadIdOrderByIdAsc(id).stream().map(this::rowView).toList();
        return new UploadView(upload.getId(), counts(rows), rows);
    }

    @Transactional(readOnly = true)
    public List<QueueView> queue() {
        return rowRepository.findByStatusOrderByTxnDateAscIdAsc(BankRowStatus.QUEUED).stream()
                .map(row -> new QueueView(row.getId(), rowView(row), candidates(row))).toList();
    }

    @Transactional
    public RowView resolve(long rowId, ResolveRequest request, AuthUser actor) {
        BankRow row = rowRepository.findById(rowId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank row not found"));
        if (row.getStatus() != BankRowStatus.QUEUED) throw new ResponseStatusException(HttpStatus.CONFLICT, "Bank row already resolved");
        Bill bill = billRepository.findByIdForUpdate(request.billId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill not found"));
        if (!bill.getFlat().getId().equals(request.flatId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bill does not belong to flat");
        billingService.createPayment(bill, row.getAmountPaise(), PaymentMethod.MANUAL, row.getReference(), row);
        row.setMatchedFlat(bill.getFlat()); row.setMatchedBill(bill); row.setStatus(BankRowStatus.RESOLVED);
        row.setResolvedBy(actor.getUser()); row.setExplanation("Manually resolved to " + bill.getFlat().getFlatNo());
        rowRepository.save(row);
        audit(row, actor.getUser(), "RESOLVE", row.getExplanation());
        return rowView(row);
    }

    @Transactional
    public RowView reject(long rowId, String reason, AuthUser actor) {
        if (reason == null || reason.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reason is required");
        BankRow row = rowRepository.findById(rowId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank row not found"));
        if (row.getStatus() != BankRowStatus.QUEUED) throw new ResponseStatusException(HttpStatus.CONFLICT, "Bank row already resolved");
        row.setStatus(BankRowStatus.REJECTED); row.setExplanation(reason); row.setResolvedBy(actor.getUser());
        rowRepository.save(row); audit(row, actor.getUser(), "REJECT", reason);
        return rowView(row);
    }

    private void audit(BankRow row, AppUser actor, String action, String reason) {
        BankRowAudit audit = new BankRowAudit();
        audit.setBankRow(row); audit.setActor(actor); audit.setAction(action); audit.setReason(reason);
        audit.setAt(dateProvider.now()); auditRepository.save(audit);
    }

    private List<Candidate> candidates(BankRow row) {
        return flatRepository.findAll().stream().filter(flat -> (row.getNarration() + " " + row.getReference())
                .toUpperCase(Locale.ROOT).contains(flat.getFlatNo().toUpperCase(Locale.ROOT))
                || (row.getUpiId() != null && row.getUpiId().equalsIgnoreCase(flat.getUpiId())))
                .map(flat -> new Candidate(flat.getId(), flat.getFlatNo(), openDue(flat))).toList();
    }

    private long openDue(Flat flat) {
        return billRepository.findByFlatIdOrderByBillMonthAsc(flat.getId()).stream()
                .mapToLong(billingService::totalDuePaise).filter(v -> v > 0).findFirst().orElse(0);
    }

    private RowView rowView(BankRow row) {
        return new RowView(row.getId(), row.getTxnDate(), row.getAmountPaise(), row.getReference(),
                row.getStatus(), row.getConfidence(), row.getExplanation());
    }

    private Map<String, Long> counts(List<RowView> rows) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (BankRowStatus status : List.of(BankRowStatus.MATCHED, BankRowStatus.QUEUED, BankRowStatus.DUPLICATE, BankRowStatus.ERROR))
            result.put(status.name().toLowerCase(Locale.ROOT), rows.stream().filter(r -> r.status() == status).count());
        return result;
    }

    private List<String[]> read(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null || !header.equals("date,amount,reference,upi_id,narration"))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid CSV header");
            List<String[]> rows = new ArrayList<>(); rows.add(header.split(",", -1));
            String line; while ((line = reader.readLine()) != null) rows.add(line.split(",", -1));
            return rows;
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to read CSV", ex);
        }
    }

    private String value(String[] values, int index) { return index < values.length ? values[index].trim() : ""; }
    private record Key(String reference, long amount, LocalDate date) {}
    private class Lookup implements BankStatementMatcher.Lookup {
        private final Set<Key> seen;
        Lookup(Set<Key> seen) { this.seen = seen; }
        public Optional<BankStatementMatcher.Row> duplicateOf(BankStatementMatcher.Row row) {
            Key key = new Key(row.reference(), row.amountPaise(), row.date());
            if (seen.contains(key)) return Optional.of(new BankStatementMatcher.Row(row.date(), row.amountPaise(), row.reference(), row.upiId(), row.narration()));
            return rowRepository.findByReferenceAndAmountPaiseAndTxnDate(row.reference(), row.amountPaise(), row.date())
                    .map(existing -> new BankStatementMatcher.Row(existing.getTxnDate(), existing.getAmountPaise(), existing.getReference(), existing.getUpiId(), existing.getNarration()));
        }
        public Optional<BankStatementMatcher.Flat> flatByNumber(String no) {
            return flatRepository.findByFlatNo(no).map(f -> new BankStatementMatcher.Flat(f.getId(), f.getFlatNo()));
        }
        public List<BankStatementMatcher.Flat> flatsByUpi(String upi) {
            return flatRepository.findByUpiId(upi).stream().map(f -> new BankStatementMatcher.Flat(f.getId(), f.getFlatNo())).toList();
        }
        public Optional<BankStatementMatcher.Bill> oldestOpenBill(long flatId) {
            return billRepository.findByFlatIdOrderByBillMonthAsc(flatId).stream()
                    .map(b -> billingService.bills(flatId).stream()
                            .filter(view -> view.id().equals(b.getId()))
                            .findFirst()
                            .map(view -> new BankStatementMatcher.Bill(view.id(), flatId,
                                    Math.max(0, view.amountPaise() - view.paidPaise()),
                                    view.lateFeePaise(), view.totalDuePaise())))
                    .flatMap(Optional::stream)
                    .filter(b -> b.totalDuePaise() > 0).findFirst();
        }
    }
    public record RowView(Long id, LocalDate date, long amountPaise, String reference, BankRowStatus status, int confidence, String explanation) {}
    public record UploadView(Long id, Map<String, Long> counts, List<RowView> rows) {}
    public record Candidate(Long flatId, String flatNo, long openDuePaise) {}
    public record QueueView(Long id, RowView row, List<Candidate> candidates) {}
    public record ResolveRequest(@Positive long flatId, @Positive long billId) {}
}
