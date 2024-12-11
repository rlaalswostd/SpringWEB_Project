package com.example.repository;

import java.util.List;

import java.util.Optional;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.entity.Store;

@Repository
public interface StoreRepository extends JpaRepository<Store, String> {

  /// 특정 사장님이 관리하는 매장 리스트 조회
  @Query("SELECT s FROM Store s WHERE s.admin.adminId = :adminId")
  List<Store> findByAdminId(@Param("adminId") Integer adminId);

  List<Store> findAll();

  // findById 메서드는 Optional<Store>를 반환합니다.
  Optional<Store> findById(String storeId);

  // 페이지네이션을 적용한 모든 매장 조회
  Page<Store> findAll(Pageable pageable);

  // adminId와 storeId로 매장을 조회하는 메소드 추가
  Optional<Store> findByStoreIdAndAdmin_AdminId(String storeId, Integer adminId);

}
