package com.example.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.entity.Category;
import com.example.entity.Menu;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    // Menu findFirstByName(String name);

    // 메뉴 이름과 storeId로 메뉴가 이미 존재하는지 확인
    Menu findByNameAndStoreId(String name, String storeId);

    Menu findByIdAndStoreId(Long id, String storeId); // storeId로 메뉴 찾기

    /// 매장 ID에 해당하는 메뉴를 페이징 처리하여 조회하는 메서드
    Page<Menu> findByStoreId(String storeId, Pageable pageable);

    // storeId와 메뉴 이름으로 검색
    public Page<Menu> findByStoreIdAndNameContainingAndCategory_CnameContaining(String storeId, String name,
            String category, Pageable pageable);

    public Page<Menu> findByStoreIdAndNameContaining(String storeId, String name, Pageable pageable);

    public Page<Menu> findByStoreIdAndCategory_CnameContaining(String storeId, String category, Pageable pageable);

    @Query("SELECT m.category FROM Menu m WHERE m.id = :menuId")
    Optional<Category> findCategoryByMenuId(@Param("menuId") Long menuId);
}
