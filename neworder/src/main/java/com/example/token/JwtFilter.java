package com.example.token;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter; // 추가

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter { // OncePerRequestFilter 상속 추가

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        /// jsp에서 json으로 변경
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        Map<String, Object> map = new HashMap<>();

        try {
            System.out.println("=============Filter=============");
            System.out.println(request.getRequestURI());
            System.out.println("=============Filter=============");

            String authHeader = request.getHeader("Authorization"); // 변수명 변경

            if (authHeader == null) {
                map.put("status", 0);
                map.put("result", "토큰 키가 없습니다");
                String json = objectMapper.writeValueAsString(map);
                response.getWriter().write(json);
                return;
            }

            if (authHeader.length() <= 0) {
                map.put("status", 0);
                map.put("result", "토큰 값이 없습니다");
                String json = objectMapper.writeValueAsString(map);
                response.getWriter().write(json);
                return;
            }

            if (!authHeader.startsWith("Bearer ")) { // 수정: Bearer 체크 로직 반대로
                map.put("status", 0);
                map.put("result", "토큰 구조가 다릅니다");
                String json = objectMapper.writeValueAsString(map);
                response.getWriter().write(json);
                return;
            }

            // 실제 토큰
            String token = authHeader.substring(7);

            // 토큰 검증이 유효하지 않으면 exception 발생하게
            Claims  claims = jwtUtil.validate(token);
            String email = claims.get("email",String.class); // jwtUtil 인스턴스 메서드로 호출
            request.setAttribute("email", email); // 키값을 문자열로 변경

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            map.put("status", -1);
            map.put("result", "토큰 값이 유효하지 않습니다");

            // map-> json으로 변경하기
            String json = objectMapper.writeValueAsString(map);
            response.getWriter().write(json);
        }
    }
}