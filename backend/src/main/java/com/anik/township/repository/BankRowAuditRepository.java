package com.anik.township.repository;

import com.anik.township.domain.BankRowAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankRowAuditRepository extends JpaRepository<BankRowAudit, Long> {
}
