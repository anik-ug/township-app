package com.anik.township.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bank_row_audit")
@Getter
@Setter
@NoArgsConstructor
public class BankRowAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bank_row_id", nullable = false)
    private BankRow bankRow;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    private AppUser actor;
    @Column(nullable = false, length = 16)
    private String action;
    @Column(length = 255)
    private String reason;
    @Column(nullable = false)
    private OffsetDateTime at;
}
