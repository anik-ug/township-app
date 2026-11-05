package com.anik.township.repository;

import com.anik.township.domain.Payment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByBillIdOrderByPaidAtAscIdAsc(Long billId);

    List<Payment> findByBillFlatIdOrderByPaidAtAscIdAsc(Long flatId);
}
