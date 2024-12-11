package com.example.token;

import static io.jsonwebtoken.Jwts.builder;
import static io.jsonwebtoken.Jwts.parser;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JWTUtil {
    
    /// 토큰 생성시 사용할 보안키
    final String KEYCODE = "felkj34#$#_%fejkr4352o9ui432908u432jfi2oj23r53232";
    byte[] securityKeyBytes = Base64.getEncoder().encode(KEYCODE.getBytes());
    SecretKey key = Keys.hmacShaKeyFor(securityKeyBytes);

    // 토큰 만료시간 설정 ex) 현재 시간에서 4시간 후
    LocalDateTime expiredAt = LocalDateTime.now().plusHours(4);
    Date  expiredAtDate = Date.from(expiredAt.atZone(ZoneId.systemDefault()).toInstant());

// 로그인 후 JWT 생성 메서드
    public String createTokenForAdmin(int adminId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("adminId", adminId);  // adminId 추가

        return createToken(claims);  // 생성된 JWT 반환
    }


    // 토큰 생성 메소드(생성할 데이터를 map에 전달하면 토큰을 발생)
    public String createToken(Map<String, Object> map){
        // map에 있는 내용을 key에 있는 암호키를 이용해서 HS256알고리즘으로 4시간짜리 토큰을 생성
        return builder().signWith(key, SIG.HS256).claims(map).expiration(expiredAtDate).compact();
    }

    // 토큰 검증 메소드
    public Claims validate(String token) {
        try {
            JwtParser parser = parser().verifyWith(key).build();
            Jws<Claims> result = parser.parseSignedClaims(token);
            return result.getPayload();
        }
        catch(Exception e) {
            if(e instanceof SignatureException){
                throw new RuntimeException("효력없는 토큰입니다.");
            }
            else if (e instanceof ExpiredJwtException) {
                throw new RuntimeException("만료시간이 종료 되었습니다.");
            }
            else {
                throw new RuntimeException("사용불가 토큰입니다.");
            }
        }
    }

}