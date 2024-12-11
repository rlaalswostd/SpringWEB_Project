package com.example.restcontroller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import com.example.entity.Admin;
import com.example.repository.AdminRepository;
import com.example.token.JWTUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/seller")
public class SellerRestController {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private JWTUtil jwtUtil; /// JWT 토큰을 생성하고 검증
        
    BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();

    

    // 판매자 로그인 처리(post)
    // => 127.0.0.1:8080/ROOT/api/seller/login.do
    @PostMapping("/login.do")
    public Map<String,Object> loginPOST(@RequestBody Admin obj) {
        Map<String, Object> map = new HashMap<>();
        try {
            // AdminRepository에서 adminId로 Admin 객체를 조회합니다.
            Admin ret = adminRepository.findById(obj.getAdminId()).orElse(null);

            // 로그인 실패
            if (ret == null) {
                map.put("status", 0);
                map.put("message", "Admin not found");
                return map;
            }

            // 비밀번호 비교 (암호화된 비밀번호와 비교하는 방식)
            if (bcpe.matches(obj.getPassword(), ret.getPassword())) {
                // 비밀번호가 맞다면, 토큰 생성
                Map<String, Object> tokenData = new HashMap<>();
                tokenData.put("ID", ret.getAdminId());
                tokenData.put("ROLE", ret.getRole());

                // JWT 토큰 발행 (jwtUtil은 실제 사용되는 서비스에서 JWT를 생성하는 유틸 클래스일 것입니다)
                String tokenString = jwtUtil.createToken(tokenData);

                // 성공 시 상태와 토큰 반환
                map.put("status", 200);
                map.put("result", tokenString);
            } else {
                // 비밀번호 불일치
                map.put("status", 0);
                map.put("message", "Invalid password");
            }
        } catch (Exception e) {
            map.put("status", -1);
            map.put("message", "An error occurred: " + e.getMessage());
            e.printStackTrace(); // 콘솔에 에러 출력
        }
        return map;
    }

    // 판매자 등록 
    

}
