package com.anik.township.repository;

import com.anik.township.domain.BankUpload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankUploadRepository extends JpaRepository<BankUpload, Long> {
}
