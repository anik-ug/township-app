package com.anik.township.repository;

import com.anik.township.domain.Complaint;
import com.anik.township.domain.ComplaintStatus;
import java.util.Collection;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    long countByFlatIdAndStatusIn(Long flatId, Collection<ComplaintStatus> statuses);

    long countByAssigneeIdAndStatusIn(Long assigneeId, Collection<ComplaintStatus> statuses);

    List<Complaint> findByFlatIdOrderByCreatedAtDesc(Long flatId);

    List<Complaint> findByFlat_TowerAndCategoryAndStatusNotInAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(
            String tower, String category, Collection<ComplaintStatus> statuses, OffsetDateTime createdAt);

    List<Complaint> findByStatusInOrderByCreatedAtAsc(Collection<ComplaintStatus> statuses);
}
