package com.anik.township.repository;

import com.anik.township.domain.Payment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import java.time.OffsetDateTime;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByBillIdOrderByPaidAtAscIdAsc(Long billId);

    List<Payment> findByBillFlatIdOrderByPaidAtAscIdAsc(Long flatId);

    @EntityGraph(attributePaths = {"bill", "bill.flat"})
    List<Payment> findAllByPaidAtLessThanEqual(OffsetDateTime paidAt);

    @Modifying
    @Query("delete from Payment p where p.bankRow is not null")
    void deleteBankPayments();
}
