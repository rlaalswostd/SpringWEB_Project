package com.example.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orderitem")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {  
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId; /// 주문 항목 번호

    @ManyToOne
    @JoinColumn(name = "order_id", referencedColumnName = "order_id", nullable = false)
    private Orders order; // 주문번호 (ORDERS 테이블과 관계)

    @ManyToOne
    @JoinColumn(name = "menu_id", referencedColumnName = "menu_id", nullable = false)
    private Menu menu; // 메뉴번호 (MENU 테이블과 관계)

    @Column(nullable = false)
    private int quantity; // 수량

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice; // 단가

    @Column(columnDefinition = "text")
    private String request; // 요청사항
}
