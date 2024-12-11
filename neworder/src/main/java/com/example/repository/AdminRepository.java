package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {
    /// Admin findByAdminId(Integer adminId);

    Object findByEmail(String email);
}
