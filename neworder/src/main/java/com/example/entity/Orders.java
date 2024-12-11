package com.example.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "orders")  // 테이블 이름을 명시적으로 ORDERS로 지정
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Orders {

    @Id
    @Column(name = "order_id", length = 16) // orderId는 수동으로 생성됨
    private Long orderId;  // Long 타입으로 수동 생성된 주문번호 사용

    @ManyToOne
    @JoinColumn(name = "table_id", referencedColumnName = "table_id", nullable = false)
    private Tables tables; // 테이블 번호 (외래 키)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status; // 주문 상태

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount; // 총금액

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @CreationTimestamp
    @Column(name = "created_at")
    private Date createdAt; // 생성일

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems; // 주문 항목들 (Cascade 설정)

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")  // 원하는 날짜 형식 지정
    @Column(name = "payment_time")
    private Date paymentTime; // 결제 완료 시간 (계산 완료 시점)


    // Enum 상태 정의
    public enum OrderStatus {
        ORDERED, COMPLETED, CANCELLED
    }

}
