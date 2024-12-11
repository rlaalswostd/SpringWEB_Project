package com.example.neworder;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/// 자동 import => alt+shift+o
@SpringBootApplication // @NeworderApplication을 @SpringBootApplication으로 수정

// 컨트롤러 위치 설정(컨트롤러 추가, 애매하면 컨트롤러에 추가해주면 됨)
@ComponentScan(basePackages = {
		"com.example.controller",
		"com.example.restcontroller",
		"com.example.security",
		"com.example.token", // token 설정함
		"com.example.service",
		"com.example.config"
})

// mapper 위치 설정
@MapperScan(basePackages = { "com.example.mapper" })

// 엔티티 위치 설정
@EntityScan(basePackages = { "com.example.entity" })

// 저장소의 위치 설정
@EnableJpaRepositories(basePackages = { "com.example.repository" })
public class NeworderApplication {

	public static void main(String[] args) {
		SpringApplication.run(NeworderApplication.class, args);
	}

}