package com.example.entity;
import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@Table(name = "menu")
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "category_id", nullable = false)
    private Category category;

    @Column(name = "store_id")
    private String storeId;

    private String name;

    @Column(name = "price")
    private BigDecimal price;
    
    @Column(name = "is_available")
    private Boolean isAvailable;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.REMOVE)  // CASCADE 설정
    private List<OrderItem> orderItems;  // 메뉴에 연관된 ORDERITEM을 자동 삭제하도록 설정

}
