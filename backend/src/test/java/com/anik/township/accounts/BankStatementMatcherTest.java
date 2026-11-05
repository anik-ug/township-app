package com.anik.township.accounts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Test;

class BankStatementMatcherTest {
    private final BankStatementMatcher matcher = new BankStatementMatcher();
    private final FakeLookup lookup = new FakeLookup();

    @Test
    void exactMatchIncludesLateFee() {
        lookup.flat("T12-0408").bill(1, 1000, 77, 1077);
        var result = match("T12-0408", 1077, null, "");
        assertEquals(BankStatementMatcher.Status.MATCHED, result.status());
        assertEquals(95, result.confidence());
        assertTrue(result.explanation().contains("late fee 0.77"));
    }

    @Test
    void principalOnlyIsQueuedWithLateFeeNote() {
        lookup.flat("T12-0408").bill(1, 1000, 77, 1077);
        var result = match("T12-0408", 1000, null, "");
        assertEquals(BankStatementMatcher.Status.QUEUED, result.status());
        assertTrue(result.explanation().contains("late fee is outstanding"));
    }

    @Test
    void partialPaymentFiveThousandIsQueued() {
        lookup.flat("T21-1601").bill(1, 566000, 1489, 567489);
        assertEquals(BankStatementMatcher.Status.QUEUED, match("T21-1601", 500000, null, "").status());
    }

    @Test
    void wrongAmountIsQueued() {
        lookup.flat("T21-1601").bill(1, 566000, 1489, 567489);
        assertEquals(BankStatementMatcher.Status.QUEUED, match("T21-1601", 600000, null, "").status());
    }

    @Test
    void supportsMissingHyphenAndMultipleFlats() {
        lookup.flat("T21-1601").bill(1, 1000, 0, 1000);
        lookup.flat("T12-0408").bill(2, 1000, 0, 1000);
        assertEquals(BankStatementMatcher.Status.MATCHED, match("T211601", 1000, null, "").status());
        assertEquals(BankStatementMatcher.Status.QUEUED, match("x", 1000, null, "T21-1601 T12-0408").status());
    }

    @Test
    void upiOnlyAndSharedUpiRules() {
        lookup.flat("T21-1601").upi("shared").bill(1, 1000, 0, 1000);
        assertEquals(BankStatementMatcher.Status.MATCHED, match("x", 1000, "shared", "").status());
        lookup.flat("T12-0408").upi("shared");
        assertEquals(BankStatementMatcher.Status.QUEUED, match("x2", 1000, "shared", "").status());
    }

    @Test
    void duplicateWithinFileIsDetected() {
        var row = new BankStatementMatcher.Row(LocalDate.now(), 1000, "x", null, "");
        lookup.seenInFile = row;
        assertEquals(BankStatementMatcher.Status.DUPLICATE, matcher.match(row, lookup).status());
    }

    @Test
    void duplicateAgainstExistingRowIsDetected() {
        var row = new BankStatementMatcher.Row(LocalDate.now(), 1000, "existing", null, "");
        lookup.existing = row;
        assertEquals(BankStatementMatcher.Status.DUPLICATE, matcher.match(row, lookup).status());
    }

    @Test
    void unknownRowIsQueuedWithoutCandidate() {
        var result = match("UNKNOWN", 1000, "unknown@bank", "unidentified transfer");
        assertEquals(BankStatementMatcher.Status.QUEUED, result.status());
        assertEquals(0, result.confidence());
        assertEquals("No flat number or known UPI ID found.", result.explanation());
    }

    @Test
    void oldestOpenBillIsSelected() {
        lookup.flat("T12-0408").bill(1, 1000, 0, 1000).bill(2, 2000, 0, 2000);
        var result = match("T12-0408", 1000, null, "");
        assertEquals(1L, result.billId());
    }

    private BankStatementMatcher.Result match(String reference, long amount, String upi, String narration) {
        return matcher.match(new BankStatementMatcher.Row(LocalDate.now(), amount, reference, upi, narration), lookup);
    }

    private static class FakeLookup implements BankStatementMatcher.Lookup {
        final Map<String, BankStatementMatcher.Flat> flats = new HashMap<>();
        final Map<Long, List<BankStatementMatcher.Bill>> bills = new HashMap<>();
        final Map<String, List<BankStatementMatcher.Flat>> upis = new HashMap<>();
        BankStatementMatcher.Row seenInFile;
        BankStatementMatcher.Row existing;
        FlatBuilder flat(String no) {
            BankStatementMatcher.Flat flat = new BankStatementMatcher.Flat(flats.size() + 1L, no);
            flats.put(no, flat);
            return new FlatBuilder(flat);
        }
        public Optional<BankStatementMatcher.Row> duplicateOf(BankStatementMatcher.Row row) {
            if (seenInFile != null && seenInFile.reference().equals(row.reference())) {
                return Optional.of(seenInFile);
            }
            return Optional.ofNullable(existing);
        }
        public Optional<BankStatementMatcher.Flat> flatByNumber(String no) { return Optional.ofNullable(flats.get(no)); }
        public List<BankStatementMatcher.Flat> flatsByUpi(String upi) { return upis.getOrDefault(upi, List.of()); }
        public Optional<BankStatementMatcher.Bill> oldestOpenBill(long id) {
            return bills.getOrDefault(id, List.of()).stream().filter(b -> b.totalDuePaise() > 0)
                    .min(Comparator.comparingLong(BankStatementMatcher.Bill::id));
        }
        class FlatBuilder {
            final BankStatementMatcher.Flat flat;
            FlatBuilder(BankStatementMatcher.Flat flat) { this.flat = flat; }
            FlatBuilder upi(String upi) { upis.computeIfAbsent(upi, x -> new ArrayList<>()).add(flat); return this; }
            FlatBuilder bill(long id, long principal, long fee, long total) {
                bills.computeIfAbsent(flat.id(), x -> new ArrayList<>())
                        .add(new BankStatementMatcher.Bill(id, flat.id(), principal, fee, total));
                return this;
            }
        }
    }
}
