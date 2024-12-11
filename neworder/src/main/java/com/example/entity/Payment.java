package com.example.entity;
import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId; /// 결제번호

    @ManyToOne
    @JoinColumn(name = "contract_id", referencedColumnName = "contract_id", nullable = false)
    private Contract contract; // 계약번호 (CONTRACT 테이블과 관계)

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount; // 결제 금액

    @Column(name = "payment_date", nullable = false)
    private Date paymentDate; // 결제일

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // 결제 상태 (COMPLETED, FAILED)

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod; // 결제 방법

    // Enum for PaymentStatus
    public enum PaymentStatus {
        COMPLETED, FAILED
    }
}
