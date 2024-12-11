package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Menuimage;

@Repository
public interface MenuImageRepository extends JpaRepository<Menuimage, Long> {
    List<Menuimage> findByMenu_Id(Long menuId);//

    

}