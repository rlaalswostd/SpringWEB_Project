package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//1
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping("/**") // 모든 경로에 대해 CORS 설정 적용함
            .allowedOrigins("http://localhost:3000","http://localhost:3001") // React 개발 서버의 주소 허용
            .allowedHeaders("*")
            .exposedHeaders("*") // 추가: 모든 헤더 노출 허용
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드들
            .allowCredentials(true) // true에서 false로 변경
            .maxAge(3600); // preflight 캐시 시간 추가

    }

}
