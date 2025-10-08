package com.equip.sp_board.auth.jwt;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {
    private final SecretKey secretKey;

    public JwtProvider(@Value("${spring.jwt.secret}")String secret) {
        // String 타입의 secret 을 객체변수(secretKey) 로 암호화
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    // Jwt 생성
    public String createJwt(String tokenId, String tokenType, String username, String role, Long expiredMs){
        return Jwts.builder()
                .claim("tokenId", tokenId)
                .claim("tokenType",tokenType)
                .claim("username",username)
                .claim("role",role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    // 토큰 만료 여부
    public Boolean isExpired(String token) {
        try {
            // parseSignedClaims 시 이미 만료 검사 포함
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return false; // 예외 없으면 만료되지 않음
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return true;  // 만료됨
        }
    }

    public String getTokenId(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("tokenId", String.class);
    }

    public String getTokenType(String token){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("tokenType", String.class);
    }

    public String getUsername(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
    }

    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }
}
