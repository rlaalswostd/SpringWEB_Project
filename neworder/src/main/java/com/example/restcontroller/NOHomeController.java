package com.example.restcontroller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

///http://localhost:8080/ROOT/neworderhome/

@RestController
@RequestMapping(value = "/neworderhome")
@RequiredArgsConstructor

// 관리자 Home
public class NOHomeController {

    @GetMapping("/") // 메인홈
    public Map<String, Object> neworderhomeGET() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Welcome to NewOrder Site");

        return response;
    }

    @GetMapping("/admin-login")
    public Map<String, Object> adminLoginGET() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "ADMIN PAGE");

        return response;
    }
    @GetMapping("/manager-login")
    public Map<String, Object> managerLogin(@RequestParam("email") String email, @RequestParam("password") String password) {
        Map<String, Object> response = new HashMap<>();
    
        // 로그인 인증 로직
        // email과 password를 확인하고, 로그인 성공 시 adminId와 token을 반환하도록 함
    
        Long adminId = 1L;  // 예시: 로그인된 adminId
        Long storeId = 123L;  // 예시: 로그인된 storeId
        String token = "someGeneratedToken";  // 예시: 생성된 토큰
    
        response.put("status", "success");
        response.put("adminId", adminId);  // adminId를 포함
        response.put("storeId", storeId);  // storeId
        response.put("token", token);  // 로그인 시 사용될 토큰
    
        return response;
    }

}
