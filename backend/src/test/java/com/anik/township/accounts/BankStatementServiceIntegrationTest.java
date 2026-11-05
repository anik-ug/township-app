package com.anik.township.accounts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.anik.township.auth.AuthUser;
import com.anik.township.billing.BillingService;
import com.anik.township.domain.PaymentMethod;
import com.anik.township.repository.AppUserRepository;
import com.anik.township.repository.PaymentRepository;
import com.anik.township.repository.BankRowRepository;
import com.anik.township.repository.BankUploadRepository;
import com.anik.township.repository.BankRowAuditRepository;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@Transactional
@Rollback
class BankStatementServiceIntegrationTest {
    @Autowired BankStatementService service;
    @Autowired AppUserRepository users;
    @Autowired PaymentRepository payments;
    @Autowired BillingService billingService;
    @Autowired BankRowRepository bankRows;
    @Autowired BankUploadRepository uploads;
    @Autowired BankRowAuditRepository audits;
    @Autowired TransactionTemplate transactionTemplate;

    @BeforeEach
    void clearImportedBankDataInsideRollback() {
        payments.deleteBankPayments();
        audits.deleteAllInBatch();
        bankRows.deleteAllInBatch();
        uploads.deleteAllInBatch();
    }

    @AfterTransaction
    void cleanImportedBankDataAfterTest() {
        transactionTemplate.executeWithoutResult(status -> {
            payments.deleteBankPayments();
            audits.deleteAllInBatch();
            bankRows.deleteAllInBatch();
            uploads.deleteAllInBatch();
        });
    }

    @Test
    @Transactional
    @Rollback
    void sampleUploadKeepsQueuedRowsOutOfPayments() throws Exception {
        try (InputStream input = new ClassPathResource("sample_bank.csv").getInputStream()) {
            var file = new MockMultipartFile("file", "sample_bank.csv", "text/csv", input);
            var result = service.upload(file, new AuthUser(users.findById(43L).orElseThrow()));
            assertEquals(6L, result.counts().get("queued"));
            assertEquals(1L, result.counts().get("duplicate"));
            assertEquals(1L, result.counts().get("error"));
        }
    }

    @Test
    @Transactional
    @Rollback
    void uploadingSampleTwicePostsEachPaymentOnlyOnce() throws Exception {
        long before = payments.count();
        AuthUser accounts = new AuthUser(users.findById(43L).orElseThrow());
        uploadSample(accounts);
        long afterFirst = payments.count();
        uploadSample(accounts);
        assertEquals(5L, afterFirst - before);
        assertEquals(afterFirst, payments.count());
    }

    @Test
    @Transactional
    @Rollback
    void queuedRowsCreateNoPayment() throws Exception {
        long before = payments.count();
        var result = uploadCsv("date,amount,reference,upi_id,narration\n"
                + "2026-11-03,5000.00,PART-T21-1601,,partial payment");
        assertEquals(1L, result.counts().get("queued"));
        assertEquals(before, payments.count());
    }

    @Test
    @Transactional
    @Rollback
    void resolveWorksOnceAndSecondResolveReturns409() {
        var result = uploadCsv("date,amount,reference,upi_id,narration\n"
                + "2026-11-03,5000.00,PART-T21-1601,,partial payment");
        long rowId = result.rows().get(0).id();
        var accounts = new AuthUser(users.findById(43L).orElseThrow());
        var resolved = service.resolve(rowId, new BankStatementService.ResolveRequest(3, 9), accounts);
        assertEquals(com.anik.township.domain.BankRowStatus.RESOLVED, resolved.status());
        var exception = assertThrows(ResponseStatusException.class,
                () -> service.resolve(rowId, new BankStatementService.ResolveRequest(3, 9), accounts));
        assertEquals(409, exception.getStatusCode().value());
    }

    @Test
    @Transactional
    @Rollback
    void resolvingWithAnotherFlatsBillIsRejected() {
        var result = uploadCsv("date,amount,reference,upi_id,narration\n"
                + "2026-11-03,5000.00,PART-T21-1601,,partial payment");
        var exception = assertThrows(ResponseStatusException.class,
                () -> service.resolve(result.rows().get(0).id(),
                        new BankStatementService.ResolveRequest(3, 2),
                        new AuthUser(users.findById(43L).orElseThrow())));
        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    @Transactional
    @Rollback
    void malformedAmountIsErrorAndRestIsProcessed() {
        var result = uploadCsv("date,amount,reference,upi_id,narration\n"
                + "2026-11-03,not-money,BAD,,bad\n"
                + "2026-11-03,5000.00,PART-T21-1601,,partial");
        assertEquals(1L, result.counts().get("error"));
        assertEquals(1L, result.counts().get("queued"));
        assertEquals("not-money", result.rows().get(0).rawAmount());
        assertEquals(true, result.rows().get(0).explanation().contains("at most 2 decimals"));
        assertEquals(true, result.rows().get(0).explanation().contains("Raw amount: not-money"));
    }

    @Test
    @Transactional
    @Rollback
    void missingHeaderIsRejected() {
        assertThrows(ResponseStatusException.class,
                () -> uploadCsv("wrong,header\n2026-11-03,5000.00"));
    }

    @Test
    @Transactional
    @Rollback
    void fiveThousandAndOneRowsAreRejected() {
        StringBuilder csv = new StringBuilder("date,amount,reference,upi_id,narration\n");
        for (int i = 0; i < 5_001; i++) {
            csv.append("2026-11-03,1.00,ROW-").append(i).append(",,unknown\n");
        }
        assertThrows(ResponseStatusException.class, () -> uploadCsv(csv.toString()));
    }

    @Test
    @Transactional
    @Rollback
    void threeDecimalsAndNegativeAmountsAreErrors() {
        var result = uploadCsv("date,amount,reference,upi_id,narration\n"
                + "2026-11-03,1.001,THREE,,bad\n"
                + "2026-11-03,-1.00,NEGATIVE,,bad");
        assertEquals(2L, result.counts().get("error"));
    }

    @Test
    @Transactional
    @Rollback
    void seededAamirOctoberDueRemainsUnchanged() {
        assertEquals(567489, billingService.bills(3L).stream()
                .filter(bill -> bill.billMonth().equals(LocalDate.of(2026, 10, 1)))
                .findFirst().orElseThrow().totalDuePaise());
    }

    private BankStatementService.UploadView uploadSample(AuthUser actor) throws Exception {
        try (InputStream input = new ClassPathResource("sample_bank.csv").getInputStream()) {
            return service.upload(new MockMultipartFile("file", "sample_bank.csv", "text/csv", input), actor);
        }
    }

    private BankStatementService.UploadView uploadCsv(String csv) {
        return service.upload(new MockMultipartFile("file", "test.csv", "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)), new AuthUser(users.findById(43L).orElseThrow()));
    }
}
