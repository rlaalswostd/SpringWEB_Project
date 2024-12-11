package com.example.restcontroller;

import com.example.entity.OrderItem;
import com.example.repository.OrderItemRepository;
import com.example.service.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/orders")
public class OrderRestController {

    private final OrderItemRepository orderItemRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    public OrderRestController(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    /// OrderItem에서 필요한 필드만 조회하여 반환
    // 127.0.0.1:8080/ROOT/api/orders/items/groupedByStore/1
    @GetMapping("/items/groupedByStore/{storeId}")
    public List<Object[]> getGroupedOrderItemDetailByStoreId(@PathVariable Long storeId) {
        return orderItemRepository.findGroupedOrderItemDetailByStoreId(storeId);
    }

    // 특정 tableId에 대한 OrderItem 목록 조회
    // 127.0.0.1:8080/ROOT/api/orders/items/5
    @GetMapping("/items/byStore/{storeId}/table/{tableId}")
    public List<Object[]> getOrderItemsByStoreIdAndTableId(@PathVariable Long storeId, @PathVariable Integer tableId) {
        return orderItemRepository.findOrderItemsByStoreIdAndTableId(storeId, tableId);
    }

    // 특정 주문 번호에 해당하는 OrderItem 조회
    // 127.0.0.1:8080/ROOT/api/orders/items/byOrderId
    @GetMapping("/items/byOrderId")
    public List<OrderItem> getOrderItemsByOrderId(@RequestParam Long orderId) {
        return orderItemRepository.findByOrder_OrderId(orderId);

    }

    /// 매장{storeID} 주문 내역 전체 조회
    @GetMapping("/payment/history/{storeId}")
    public Page<Map<String, Object>> getCompletedOrders(
            @PathVariable String storeId,
            Pageable pageable) {
        return orderService.getCompletedOrders(storeId, pageable);
    }
}
