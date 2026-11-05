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
@Table(name = "flat")
@Getter
@Setter
@NoArgsConstructor
public class Flat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 8)
    private String tower;

    @Column(name = "flat_no", nullable = false, unique = true, length = 16)
    private String flatNo;

    @Column(name = "area_sqft", nullable = false)
    private int areaSqft;

    @Column(name = "upi_id", length = 64)
    private String upiId;
}
