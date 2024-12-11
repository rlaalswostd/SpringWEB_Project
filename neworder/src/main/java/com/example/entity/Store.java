package com.example.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "store")
@NoArgsConstructor
@AllArgsConstructor
public class Store {

    @Id
    @Column(name = "store_id", length = 12)
    private String storeId; // 매장 고유번호

    @ManyToOne
    @JoinColumn(name = "admin_id", referencedColumnName = "admin_id", nullable = false)
    private Admin admin; /// 매장 관리자 (외래 키)

    @Column(name = "store_name", nullable = false, length = 100)
    private String storeName; // 매장명

    @Column(nullable = false, length = 255)
    private String address; // 주소

    @Column(nullable = false, length = 20)
    private String phone; // 전화번호

    @Column(name = "is_active", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isActive = true; // 운영 상태
}
