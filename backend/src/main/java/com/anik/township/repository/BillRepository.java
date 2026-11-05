package com.anik.township.repository;

import com.anik.township.domain.Bill;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findByFlatIdOrderByBillMonthDesc(Long flatId);
    List<Bill> findByFlatIdOrderByBillMonthAsc(Long flatId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bill b join fetch b.flat where b.id = :id")
    java.util.Optional<Bill> findByIdForUpdate(@Param("id") Long id);
}
