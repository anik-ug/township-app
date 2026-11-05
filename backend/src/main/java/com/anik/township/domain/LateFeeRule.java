package com.anik.township.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "late_fee_rule")
@Getter
@Setter
@NoArgsConstructor
public class LateFeeRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "grace_days", nullable = false)
    private int graceDays;

    @Column(name = "annual_rate_bps", nullable = false)
    private int annualRateBps;

    @Column(name = "cap_paise")
    private Long capPaise;

    @Column(nullable = false)
    private boolean active;
}
