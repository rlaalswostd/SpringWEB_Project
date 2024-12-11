package com.example.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tables") // 테이블 이름 지정
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tables {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID 자동 생성
    @Column(name = "table_id")
    private Integer tableId; // 테이블 ID (기본 키)

    @ManyToOne
    @JoinColumn(name = "store_id", referencedColumnName = "store_id", nullable = false)
    private Store store; // 매장 번호 (외래 키)

    @Column(name = "table_number", nullable = false, length = 10)
    private String tableNumber; // 테이블 번호 (표시용)

    @Column(name = "is_occupied", nullable = false)
    private Boolean isOccupied = false; // 사용 여부 (기본값: false)

    @OneToMany(mappedBy = "tables", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Orders> orders; // 테이블에 관련된 주문 (Cascade 설정)
}
