package com.example.repository;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.entity.Menu;
import com.example.entity.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
        List<OrderItem> findByOrder_OrderId(Long orderId);

        @Query("SELECT oi.order.tables.tableNumber, " +
                        "GROUP_CONCAT(oi.menu.name) AS names, " +
                        "GROUP_CONCAT(oi.quantity) AS quantities, " + // quantity 추가
                        "oi.order.createdAt, " +
                        "oi.order.status, " +
                        "oi.order.orderId " +
                        "FROM OrderItem oi " +
                        "WHERE oi.order.tables.store.storeId = :storeId " +
                        "GROUP BY oi.order.tables.tableId, oi.order.createdAt, oi.order.status, oi.order.orderId " +
                        "ORDER BY oi.order.createdAt DESC")
        List<Object[]> findGroupedOrderItemDetailByStoreId(@Param("storeId") Long storeId);

        /// tableId에 해당하는 menu name만 분리하여 반환
        @Query("SELECT oi.order.tables.tableId, oi.menu.name, oi.quantity " +
                        "FROM OrderItem oi " +
                        "WHERE oi.order.tables.store.storeId = :storeId " + // storeId로 필터링
                        "AND oi.order.tables.tableId = :tableId " +
                        "ORDER BY oi.order.createdAt DESC") // 최근 주문이 위로
        List<Object[]> findOrderItemsByStoreIdAndTableId(@Param("storeId") Long storeId,
                        @Param("tableId") Integer tableId);

        boolean existsByMenuId(Long menuId); // 메뉴 ID로 장바구니에 있는지 확인

        // 새로운 메서드: 메뉴와 관련된 OrderItem을 찾는 메서드
        List<OrderItem> findByMenu(Menu menu); // 메뉴로 OrderItem을 조회
}
