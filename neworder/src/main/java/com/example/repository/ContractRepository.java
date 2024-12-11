package com.example.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.entity.Contract;
//1
@Repository
public interface ContractRepository extends JpaRepository<Contract, Integer> {
    Page<Contract> findAll(Pageable pageable); // Pageable을 받는 메서드

    @Transactional
    @Modifying
    @Query("UPDATE Contract c SET c.status = 'EXPIRED' WHERE c.endDate < :today AND c.status = 'ACTIVE'")
    int updateExpiredContracts(LocalDate today);
}
