package com.example.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Getter
@Setter
@Table(name = "contract")
@NoArgsConstructor
@AllArgsConstructor
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_id")
    private Integer contractId; /// 계약번호

    @ManyToOne
    @JoinColumn(name = "store_id", referencedColumnName = "store_id", nullable = false)
    private Store store; // 매장번호 (외래 키)

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate; // 계약시작일

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate; // 계약종료일

    @Column(name = "monthly_fee", nullable = false)
    private BigDecimal monthlyFee; // 월 이용료

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status; // 상태 (ACTIVE, EXPIRED)

    // Enum 타입 정의
    public enum Status {
        ACTIVE,
        EXPIRED
    }
}
