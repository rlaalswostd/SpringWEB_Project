package com.example.entity;

public interface AdminProjection {
    
    Integer getAdminId(); // 관리자 고유번호
    String getAdminName(); // 관리자명
    String getEmail(); // 이메일
    Admin.Role getRole(); /// 권한 (SUPER_ADMIN, STORE_ADMIN)

}
