package com.example.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import com.example.mapper.OrderMapper;

@Service
public class OrderService {
    @Autowired
    private OrderMapper orderMapper;

    /// 결제 내역 가져오기 (storeId에 해당하는 결제 내역만 조회)
    public Map<String, Object> getPaymentHistoryByStoreId(Long storeId, int page, int pageSize, String startDate,
            String endDate) {
        Map<String, Object> result = new HashMap<>();

        // 페이지네이션 처리
        int offset = page * pageSize;
        List<Map<String, Object>> paymentHistory = orderMapper.getPaymentHistoryByStoreId(storeId, offset, pageSize,
                startDate, endDate);
        int totalCount = orderMapper.getTotalPaymentHistoryCountByStoreId(storeId);

        result.put("paymentHistory", paymentHistory);
        result.put("totalCount", totalCount);

        return result;
    }

    /// 매장{storeID} 주문 내역 전체 조회
    public Page<Map<String, Object>> getCompletedOrders(String storeId, Pageable pageable) {
        // 주문 내역 조회 (storeId와 페이징 정보를 기반으로 완료된 주문 목록(orders)을 조회)
        List<Map<String, Object>> orders = orderMapper.findCompletedOrders(storeId, pageable);

        // 전체 주문 수 조회 (storeId의 전체 완료된 주문 수(total)반환)
        long total = orderMapper.countCompletedOrders(storeId);

        // 페이징 처리된 결과 반환
        return PageableExecutionUtils.getPage(orders, pageable, () -> total);
    }
}
