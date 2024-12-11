package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    // Category findByCname(String cname); // 카테고리 이름으로 조회

    /// 동일한 storeId와 displayOrder로 검색
    Category findByStoreIdAndDisplayOrder(String storeId, int displayOrder);

    List<Category> findByStoreIdAndDisplayOrderGreaterThan(String storeId, int displayOrder);

    Category findFirstByCname(String cname);

    // CategoryRepository에 추가할 메서드 (타입 수정)
    Category findByCnameAndStoreId(String cname, String storeId);

    List<Category> findByStoreIdOrderByDisplayOrder(String storeId);

    Category findByStoreIdAndDisplayOrder(String storeId, Integer displayOrder);}

    
