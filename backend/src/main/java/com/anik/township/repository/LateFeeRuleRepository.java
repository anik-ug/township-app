package com.anik.township.repository;

import com.anik.township.domain.LateFeeRule;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LateFeeRuleRepository extends JpaRepository<LateFeeRule, Long> {

    Optional<LateFeeRule> findFirstByActiveTrueOrderByIdAsc();
}
