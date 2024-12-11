package com.example.token;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Arrays;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<JwtFilter> filterRegistrationBean(JwtFilter jwtFilter) {
        FilterRegistrationBean<JwtFilter> filterReg = new FilterRegistrationBean<>();
        filterReg.setFilter(jwtFilter);
        
        /// 모든 요청에 대해 필터 적용
        filterReg.setUrlPatterns(Arrays.asList(
            "/ROOT/api/*"  // 모든 API 경로에 대해 필터 적용
        ));
        
        // 필터 우선순위를 높게 설정
        filterReg.setOrder(1);
        
        return filterReg;
    }
}