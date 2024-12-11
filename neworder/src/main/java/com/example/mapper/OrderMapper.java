package com.example.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;

public interface OrderMapper {

        /// 특정 storeId에 대한 결제 내역 조회 (페이지네이션 적용)
        List<Map<String, Object>> getPaymentHistoryByStoreId(
                        @Param("storeId") Long storeId,
                        @Param("offset") int offset,
                        @Param("pageSize") int pageSize,
                        @Param("startDate") String startDate, // 시작 날짜
                        @Param("endDate") String endDate); // 종료 날짜

        // 특정 storeId에 대한 결제 내역의 전체 건수 조회
        int getTotalPaymentHistoryCountByStoreId(@Param("storeId") Long storeId);

        // 결제 내역 조회 (페이지네이션 적용)
        List<Map<String, Object>> getPaymentHistory(@Param("offset") int offset, @Param("pageSize") int pageSize);

        /// 결제 내역의 전체 건수 조회
        int getTotalPaymentHistoryCount();



        // 매장{storeID} 주문 내역 전체 조회
        List<Map<String, Object>> findCompletedOrders(
                        @Param("storeId") String storeId,
                        @Param("pageable") Pageable pageable);

        // 전체 주문 수 조회
        long countCompletedOrders(@Param("storeId") String storeId);

}
