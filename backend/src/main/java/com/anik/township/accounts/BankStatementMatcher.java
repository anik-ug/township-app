package com.anik.township.accounts;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BankStatementMatcher {
    private static final Pattern FLAT_PATTERN = Pattern.compile("T\\d{2}-?\\d{4}", Pattern.CASE_INSENSITIVE);

    public Result match(Row row, Lookup lookup) {
        Optional<Row> duplicate = lookup.duplicateOf(row);
        if (duplicate.isPresent()) {
            return new Result(Status.DUPLICATE, 0, "Duplicates earlier row " + duplicate.get().reference(), null, null);
        }

        Set<String> flatNos = extractFlatNumbers(row.reference() + " " + nullToEmpty(row.narration()));
        List<Flat> flats = flatNos.stream().map(lookup::flatByNumber).flatMap(Optional::stream).toList();
        if (flats.size() == 1) {
            Flat flat = flats.get(0);
            Optional<Bill> oldest = lookup.oldestOpenBill(flat.id());
            if (oldest.isPresent()) {
                Bill bill = oldest.get();
                if (row.amountPaise() == bill.totalDuePaise()) {
                    return new Result(Status.MATCHED, 95,
                            "Flat " + flat.flatNo() + " found in reference; amount equals total due "
                                    + money(bill.totalDuePaise()) + " (principal " + money(bill.principalPaise())
                                    + " + late fee " + money(bill.lateFeePaise()) + ")",
                            flat.id(), bill.id());
                }
                String comparison = row.amountPaise() < bill.totalDuePaise() ? "less than" : "greater than";
                String principalNote = row.amountPaise() == bill.principalPaise()
                        ? " Amount equals principal only while a late fee is outstanding." : "";
                return new Result(Status.QUEUED, 40,
                        "Flat found but amount " + money(row.amountPaise()) + " is " + comparison
                                + " total due " + money(bill.totalDuePaise()) + ". "
                                + (row.amountPaise() < bill.totalDuePaise()
                                ? "Partial payment, needs accounts review." : "Amount mismatch, needs accounts review.")
                                + principalNote, flat.id(), bill.id());
            }
        } else if (flats.size() > 1) {
            return new Result(Status.QUEUED, 20, "More than one flat found: " + flatNames(flats), null, null);
        }

        if (flats.isEmpty() && row.upiId() != null && !row.upiId().isBlank()) {
            List<Flat> upiFlats = lookup.flatsByUpi(row.upiId());
            if (upiFlats.size() == 1) {
                Flat flat = upiFlats.get(0);
                Optional<Bill> oldest = lookup.oldestOpenBill(flat.id());
                if (oldest.isPresent() && oldest.get().totalDuePaise() == row.amountPaise()) {
                    Bill bill = oldest.get();
                    return new Result(Status.MATCHED, 85,
                            "UPI ID linked to " + flat.flatNo() + "; amount equals total due "
                                    + money(bill.totalDuePaise()), flat.id(), bill.id());
                }
            } else if (upiFlats.size() > 1) {
                return new Result(Status.QUEUED, 20, "UPI ID is linked to multiple flats: " + flatNames(upiFlats), null, null);
            }
        }
        return new Result(Status.QUEUED, 0, "No flat number or known UPI ID found.", null, null);
    }

    private static Set<String> extractFlatNumbers(String text) {
        Set<String> result = new LinkedHashSet<>();
        Matcher matcher = FLAT_PATTERN.matcher(text);
        while (matcher.find()) {
            String value = matcher.group().toUpperCase(Locale.ROOT);
            result.add(value.charAt(3) == '-' ? value : value.substring(0, 3) + "-" + value.substring(3));
        }
        return result;
    }

    private static String flatNames(List<Flat> flats) {
        return flats.stream().map(Flat::flatNo).sorted().reduce((a, b) -> a + ", " + b).orElse("");
    }

    private static String nullToEmpty(String value) { return value == null ? "" : value; }
    private static String money(long paise) {
        return BigDecimal.valueOf(paise, 2).setScale(2, RoundingMode.UNNECESSARY).toPlainString();
    }

    public interface Lookup {
        Optional<Row> duplicateOf(Row row);
        Optional<Flat> flatByNumber(String flatNo);
        List<Flat> flatsByUpi(String upiId);
        Optional<Bill> oldestOpenBill(long flatId);
    }
    public record Row(LocalDate date, long amountPaise, String reference, String upiId, String narration) {}
    public record Flat(long id, String flatNo) {}
    public record Bill(long id, long flatId, long principalPaise, long lateFeePaise, long totalDuePaise) {}
    public enum Status { MATCHED, QUEUED, DUPLICATE }
    public record Result(Status status, int confidence, String explanation, Long flatId, Long billId) {}
}
