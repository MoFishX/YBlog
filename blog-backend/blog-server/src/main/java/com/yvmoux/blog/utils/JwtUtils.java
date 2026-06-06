package com.yvmoux.blog.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * JWT 令牌工具类，负责 access_token 和 refresh_token 的生成、解析与验证。
 * <p>
 * access_token 用于 API 鉴权（短期有效），refresh_token 用于续签（长期有效）。
 * 签名算法为 HMAC-SHA256，密钥通过 Base64 编码存放在配置中。
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    // ==================== 公共方法 ====================

    /**
     * 生成 access_token（短期，用于 API 鉴权）。
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @param role     角色（USER / ADMIN）
     * @return JWT 格式的访问令牌
     */
    public String generateAccessToken(Long userId, String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration * 1000))
                .signWith(getKey())
                .compact();
    }

    /**
     * 生成 refresh_token（长期，用于续签 access_token）。
     *
     * @param userId 用户 ID
     * @return JWT 格式的刷新令牌
     */
    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration * 1000))
                .signWith(getKey())
                .compact();
    }

    /**
     * 从 Token 中提取用户名（subject 字段）。
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从 Token 中提取用户 ID（自定义 claim）。
     */
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    /**
     * 从 Token 中提取角色（自定义 claim）。
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * 验证 Token 是否有效（签名正确且未过期）。
     *
     * @return true 有效，false 无效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }


    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    // ==================== 私有方法 ====================

    /**
     * 从 Base64 密钥字符串构建 HMAC-SHA256 签名密钥。
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(secret));
    }

    /**
     * 从 Token 中提取指定 claim。
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    /**
     * 解析 Token 并返回所有 claims（验证签名）。
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
    }
}
