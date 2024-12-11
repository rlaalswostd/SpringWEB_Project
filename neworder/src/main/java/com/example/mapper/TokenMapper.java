package com.example.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import com.example.dto.Token;
//
@Mapper
public interface TokenMapper {
        @Insert({
                        "INSERT INTO token (no, admin_email, token, expiretime, created_at) " +
                        "VALUES (#{no}, #{adminEmail}, #{token}, #{expiretime}, #{createAt})"
        })

        public int insertToken(Token obj);

        @Delete({
                        "DELETE FROM token WHERE admin_email = #{adminEmail}"
        })
        public int deleteByAdminEmail(String adminEmail);
}