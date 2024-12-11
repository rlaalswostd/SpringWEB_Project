package com.example.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.entity.Tables;    // Tables 엔티티 import

@Repository 
public interface TablesRepository extends JpaRepository<Tables, Integer> {
    
    @Query("SELECT new com.example.entity.Tables(t.tableId, t.isOccupied, t.tableNumber, t.store.storeId) " +
            "FROM Tables t")
    List<Object[]> findTableSummary();

    // 특정 매장에서 테이블 번호 중복 확인
    Optional<Tables> findByTableNumberAndStore_StoreId(String tableNumber, String storeId);

    List<Tables> findByStore_StoreId(String storeId);  // storeId로 필터링하는 메서드
    
}