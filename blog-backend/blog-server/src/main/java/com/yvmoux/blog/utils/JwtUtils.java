package com.yvmoux.blog.utils;

import com.yvmoux.blog.constant.JwtConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtils {

    @Value("${jwt.secret:YTk3MjNhNDY5ZjEwMmI1ZDNkOGY3ZTU2YzRhOTFiOGQ2OTNjNWU4ZjA3ZjI0M2Q4YTkzYjVmN2UwNjFiOGMyYQ==}")
    private String secret;

    @Value("${jwt.expire:86400}")
    private long expire;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(secret));
    }

    public String generateAccessToken(Long userId, String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim(JwtConstants.CLAIM_USER_ID, userId)
                .claim(JwtConstants.CLAIM_ROLE, role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expire * 1000))
                .signWith(getKey())
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 7 * 24 * 3600 * 1000L))
                .signWith(getKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get(JwtConstants.CLAIM_USER_ID, Long.class);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get(JwtConstants.CLAIM_ROLE, String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
    }
}
