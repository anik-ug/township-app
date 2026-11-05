package com.anik.township.repository;

import com.anik.township.domain.ComplaintEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintEventRepository extends JpaRepository<ComplaintEvent, Long> {
}
