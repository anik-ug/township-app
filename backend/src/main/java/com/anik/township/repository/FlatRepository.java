package com.anik.township.repository;

import com.anik.township.domain.Flat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FlatRepository extends JpaRepository<Flat, Long> {
    Optional<Flat> findByFlatNo(String flatNo);
    List<Flat> findByUpiId(String upiId);
}
