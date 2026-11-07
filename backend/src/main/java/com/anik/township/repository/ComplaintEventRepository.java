package com.anik.township.repository;

import com.anik.township.domain.ComplaintEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface ComplaintEventRepository extends JpaRepository<ComplaintEvent, Long> {
    @EntityGraph(attributePaths = {"complaint"})
    @org.springframework.data.jpa.repository.Query("select e from ComplaintEvent e")
    java.util.List<ComplaintEvent> findAllWithComplaint();
}
