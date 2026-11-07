package com.anik.township.committee;

import com.anik.township.billing.LateFeeCalculator;
import com.anik.township.domain.ComplaintStatus;
import com.anik.township.domain.BankRowStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class DashboardCalculator {
    private static final Set<ComplaintStatus> OPEN_STATUSES =
            Set.of(ComplaintStatus.OPEN, ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS);

    public Dashboard calculate(
            List<BillData> bills,
            List<PaymentData> payments,
            List<ComplaintData> complaints,
            List<ComplaintEventData> events,
            List<BankRowData> bankRows,
            LocalDate asOfDate,
            OffsetDateTime generatedAt,
            int graceDays,
            int annualRateBps,
            Long capPaise,
            int slaResponseMinutes) {
        Map<Long, List<PaymentData>> paymentsByBill = payments.stream()
                .filter(p -> !p.paidAt().toLocalDate().isAfter(asOfDate))
                .collect(Collectors.groupingBy(PaymentData::billId));
        Map<Long, BillAmounts> amounts = bills.stream().collect(Collectors.toMap(
                BillData::id,
                bill -> amounts(bill, paymentsByBill.getOrDefault(bill.id(), List.of()),
                        asOfDate, graceDays, annualRateBps, capPaise)));

        LocalDate cycle = bills.stream()
                .map(BillData::billMonth)
                .filter(month -> !month.isAfter(asOfDate))
                .max(LocalDate::compareTo)
                .orElse(null);
        List<BillData> cycleBills = cycle == null ? List.of()
                : bills.stream().filter(b -> cycle.equals(b.billMonth())).toList();
        // Collections use the latest bill month on or before as-of; collected is principal only.
        long billed = cycleBills.stream().mapToLong(BillData::amountPaise).sum();
        long collected = cycleBills.stream().mapToLong(b -> principalPaid(paymentsByBill.getOrDefault(b.id(), List.of()))).sum();
        long fullyPaid = cycleBills.stream().filter(b -> amounts.get(b.id()).principalBalance() == 0)
                .map(BillData::flatNo).distinct().count();
        long onTime = cycleBills.stream().filter(b -> principalPaidBy(
                paymentsByBill.getOrDefault(b.id(), List.of()), b.dueDate()) >= b.amountPaise()).count();
        long paidByDay10 = cycleBills.stream().mapToLong(b -> principalPaidBetween(
                paymentsByBill.getOrDefault(b.id(), List.of()), b.billMonth(), b.billMonth().plusDays(9))).sum();
        Collections collections = new Collections(
                cycle, billed, collected, percentage(collected, billed),
                cycleBills.stream().map(BillData::flatNo).distinct().count(), fullyPaid,
                percentage(onTime, cycleBills.size()),
                payments.stream().filter(p -> !p.paidAt().toLocalDate().isBefore(asOfDate.minusDays(30))
                        && !p.paidAt().toLocalDate().isAfter(asOfDate))
                        .mapToLong(PaymentData::amountPaise).sum(),
                percentage(paidByDay10, billed));

        // Ageing uses computed principal plus late fee, and bills are assigned by days past due.
        Map<String, AgeingBucket> ageing = new LinkedHashMap<>();
        ageing.put("0-30", new AgeingBucket(0, 0, 0));
        ageing.put("31-60", new AgeingBucket(0, 0, 0));
        ageing.put("61-90", new AgeingBucket(0, 0, 0));
        ageing.put("90+", new AgeingBucket(0, 0, 0));
        Map<String, Map<String, Long>> flatTotals = new LinkedHashMap<>();
        for (BillData bill : bills) {
            BillAmounts billAmounts = amounts.get(bill.id());
            if (bill.billMonth().isAfter(asOfDate) || billAmounts.totalDuePaise() <= 0) {
                continue;
            }
            long daysPastDue = Math.max(0, ChronoUnit.DAYS.between(bill.dueDate(), asOfDate));
            String bucket = daysPastDue <= 30 ? "0-30" : daysPastDue <= 60 ? "31-60"
                    : daysPastDue <= 90 ? "61-90" : "90+";
            AgeingBucket current = ageing.get(bucket);
            ageing.put(bucket, new AgeingBucket(current.flatCount(), current.billCount() + 1,
                    current.totalDuePaise() + billAmounts.totalDuePaise()));
            flatTotals.computeIfAbsent(bill.flatNo(), ignored -> new LinkedHashMap<>())
                    .merge("totalDuePaise", billAmounts.totalDuePaise(), Long::sum);
        }
        Map<String, AgeingBucket> finalizedAgeing = ageing.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> new AgeingBucket(
                        (int) flatTotalsForBucket(bills, amounts, asOfDate, entry.getKey()),
                        entry.getValue().billCount(), entry.getValue().totalDuePaise()),
                (a, b) -> a, LinkedHashMap::new));
        List<FlatDue> topFlats = flatTotals.entrySet().stream()
                .map(e -> new FlatDue(e.getKey(), e.getValue().get("totalDuePaise")))
                .sorted(Comparator.comparingLong(FlatDue::totalDuePaise).reversed().thenComparing(FlatDue::flatNo))
                .limit(10).toList();
        DuesAgeing duesAgeing = new DuesAgeing(finalizedAgeing,
                finalizedAgeing.values().stream().mapToLong(AgeingBucket::totalDuePaise).sum(), topFlats);

        // Response is the first IN_PROGRESS/RESOLVED/CLOSED event; resolution is the first RESOLVED event.
        List<ComplaintData> visibleComplaints = complaints.stream()
                .filter(c -> !c.createdAt().toLocalDate().isAfter(asOfDate)).toList();
        List<ComplaintEventData> visibleEvents = events.stream()
                .filter(e -> !e.eventAt().toLocalDate().isAfter(asOfDate)).toList();
        Map<Long, List<ComplaintEventData>> eventsByComplaint = visibleEvents.stream()
                .collect(Collectors.groupingBy(ComplaintEventData::complaintId));
        long escalatedOpen = visibleComplaints.stream()
                .filter(c -> OPEN_STATUSES.contains(c.status()) && c.escalated()).count();
        Map<String, Long> byCategory = visibleComplaints.stream().collect(Collectors.groupingBy(
                ComplaintData::category, LinkedHashMap::new, Collectors.counting()));
        List<Long> responseMinutes = new ArrayList<>();
        List<Long> resolutionMinutes = new ArrayList<>();
        long responseExcluded = 0;
        long resolutionExcluded = 0;
        for (ComplaintData complaint : visibleComplaints) {
            List<ComplaintEventData> complaintEvents = eventsByComplaint.getOrDefault(complaint.id(), List.of());
            OffsetDateTime response = complaintEvents.stream()
                    .filter(e -> e.toStatus() == ComplaintStatus.IN_PROGRESS
                            || e.toStatus() == ComplaintStatus.RESOLVED || e.toStatus() == ComplaintStatus.CLOSED)
                    .map(ComplaintEventData::eventAt).min(OffsetDateTime::compareTo).orElse(null);
            OffsetDateTime resolution = complaintEvents.stream().filter(e -> e.toStatus() == ComplaintStatus.RESOLVED)
                    .map(ComplaintEventData::eventAt).min(OffsetDateTime::compareTo).orElse(null);
            if (response == null) responseExcluded++;
            else responseMinutes.add((int) ChronoUnit.MINUTES.between(complaint.createdAt(), response) * 1L);
            if (resolution == null) resolutionExcluded++;
            else resolutionMinutes.add(ChronoUnit.MINUTES.between(complaint.createdAt(), resolution));
        }
        OffsetDateTime effectiveNow = asOfDate.atTime(LocalTime.MAX).atOffset(generatedAt.getOffset());
        long slaBreaches = visibleComplaints.stream().filter(c -> OPEN_STATUSES.contains(c.status())
                && c.createdAt().plusMinutes(slaResponseMinutes).isBefore(effectiveNow)
                && eventsByComplaint.getOrDefault(c.id(), List.of()).stream().noneMatch(e ->
                        e.toStatus() == ComplaintStatus.IN_PROGRESS || e.toStatus() == ComplaintStatus.RESOLVED
                                || e.toStatus() == ComplaintStatus.CLOSED)).count();
        Complaints complaintSummary = new Complaints(
                visibleComplaints.stream().filter(c -> OPEN_STATUSES.contains(c.status())).count(),
                escalatedOpen, byCategory,
                average(responseMinutes), averageHours(resolutionMinutes), percentage(
                        visibleComplaints.stream().filter(c -> c.reopenCount() > 0).count(), visibleComplaints.size()),
                slaBreaches, responseExcluded, resolutionExcluded);

        long queued = bankRows.stream().filter(r -> r.status() == BankRowStatus.QUEUED).count();
        long queuedAmount = bankRows.stream().filter(r -> r.status() == BankRowStatus.QUEUED)
                .mapToLong(BankRowData::amountPaise).sum();
        return new Dashboard(collections, duesAgeing, complaintSummary,
                new AccountsQueue(queued, queuedAmount), asOfDate, generatedAt);
    }

    private static BillAmounts amounts(BillData bill, List<PaymentData> payments, LocalDate asOf,
            int graceDays, int annualRateBps, Long capPaise) {
        long principalPaid = principalPaid(payments);
        long balance = Math.max(0, bill.amountPaise() - principalPaid);
        long fee = LateFeeCalculator.calculate(balance, bill.dueDate(), asOf, graceDays, annualRateBps, capPaise);
        long feePaid = payments.stream().mapToLong(PaymentData::lateFeePaise).sum();
        return new BillAmounts(balance, Math.max(0, fee - feePaid), balance + Math.max(0, fee - feePaid));
    }

    private static long principalPaid(List<PaymentData> payments) {
        return payments.stream().mapToLong(p -> p.amountPaise() - p.lateFeePaise()).sum();
    }

    private static long principalPaidBy(List<PaymentData> payments, LocalDate date) {
        return principalPaid(payments.stream().filter(p -> !p.paidAt().toLocalDate().isAfter(date)).toList());
    }

    private static long principalPaidBetween(List<PaymentData> payments, LocalDate start, LocalDate end) {
        return principalPaid(payments.stream().filter(p -> !p.paidAt().toLocalDate().isBefore(start)
                && !p.paidAt().toLocalDate().isAfter(end)).toList());
    }

    private static long flatTotalsForBucket(List<BillData> bills, Map<Long, BillAmounts> amounts,
            LocalDate asOf, String bucket) {
        return bills.stream().filter(b -> !b.billMonth().isAfter(asOf) && amounts.get(b.id()).totalDuePaise() > 0)
                .filter(b -> bucket.equals(bucket(Math.max(0, ChronoUnit.DAYS.between(b.dueDate(), asOf)))))
                .map(BillData::flatNo).distinct().count();
    }

    private static String bucket(long days) {
        return days <= 30 ? "0-30" : days <= 60 ? "31-60" : days <= 90 ? "61-90" : "90+";
    }

    private static BigDecimal percentage(long numerator, long denominator) {
        return denominator == 0 ? BigDecimal.ZERO.setScale(2)
                : BigDecimal.valueOf(numerator).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal average(List<Long> values) {
        return values.isEmpty() ? BigDecimal.ZERO.setScale(2)
                : BigDecimal.valueOf(values.stream().mapToLong(Long::longValue).sum())
                        .divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal averageHours(List<Long> minutes) {
        return minutes.isEmpty() ? BigDecimal.ZERO.setScale(2)
                : BigDecimal.valueOf(minutes.stream().mapToLong(Long::longValue).sum())
                        .divide(BigDecimal.valueOf(minutes.size() * 60L), 2, RoundingMode.HALF_UP);
    }

    private record BillAmounts(long principalBalance, long lateFeeDuePaise, long totalDuePaise) {}

    public record BillData(long id, String flatNo, LocalDate billMonth, long amountPaise, LocalDate dueDate) {}
    public record PaymentData(long billId, long amountPaise, long lateFeePaise, OffsetDateTime paidAt) {}
    public record ComplaintData(long id, String category, ComplaintStatus status, boolean escalated,
            OffsetDateTime createdAt, int reopenCount) {}
    public record ComplaintEventData(long complaintId, ComplaintStatus toStatus, OffsetDateTime eventAt) {}
    public record BankRowData(BankRowStatus status, long amountPaise) {}

    public record Dashboard(Collections collections, DuesAgeing duesAgeing, Complaints complaints,
            AccountsQueue accountsQueue, LocalDate asOfDate, OffsetDateTime generatedAt) {}
    public record Collections(LocalDate billMonth, long billedPaise, long collectedPaise,
            BigDecimal collectionRatePct, long flatsBilled, long flatsFullyPaid, BigDecimal onTimeRatePct,
            long paymentsLast30DaysPaise, BigDecimal paidByDay10Pct) {}
    public record DuesAgeing(Map<String, AgeingBucket> buckets, long totalOutstandingPaise, List<FlatDue> topFlats) {}
    public record AgeingBucket(long flatCount, long billCount, long totalDuePaise) {}
    public record FlatDue(String flatNo, long totalDuePaise) {}
    public record Complaints(long openCount, long escalatedOpenCount, Map<String, Long> byCategory,
            BigDecimal avgFirstResponseMinutes, BigDecimal avgResolutionHours, BigDecimal reopenedPct,
            long slaBreaches, long responseExcludedCount, long resolutionExcludedCount) {}
    public record AccountsQueue(long queuedCount, long queuedAmountPaise) {}
}
