package com.anik.township.repository;

import com.anik.township.domain.BankRow;
import com.anik.township.domain.BankRowStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankRowRepository extends JpaRepository<BankRow, Long> {
    Optional<BankRow> findByReferenceAndAmountPaiseAndTxnDate(String reference, long amountPaise, LocalDate txnDate);
    List<BankRow> findByUploadIdOrderByIdAsc(Long uploadId);
    List<BankRow> findByStatusOrderByTxnDateAscIdAsc(BankRowStatus status);
}
