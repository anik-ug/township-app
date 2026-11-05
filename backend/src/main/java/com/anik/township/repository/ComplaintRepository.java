package com.anik.township.repository;

import com.anik.township.domain.Complaint;
import com.anik.township.domain.ComplaintStatus;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    long countByFlatIdAndStatusIn(Long flatId, Collection<ComplaintStatus> statuses);
}
