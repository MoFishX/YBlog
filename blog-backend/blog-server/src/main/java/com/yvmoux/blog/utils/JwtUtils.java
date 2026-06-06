package com.yvmoux.blog.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtils {
    
    @Value("${jwt.secret}")
    private String secret = "test";
    
    @Value("${jwt.expire}")
    private long expire = 86400;
    
    /**
     * 获取用于JWT签名和验证的HMAC SHA密钥
     *
     * @return SecretKey 对象，基于配置的secret字符串生成
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 从JWT令牌中提取用户名（Subject）
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从JWT令牌中提取指定的声明（Claim）
     *
     * @param token JWT令牌
     * @param claimsResolver 用于从Claims中提取特定值的函数
     * @param <T> 返回值的类型
     * @return 提取的声明值
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析JWT令牌并获取所有声明（Payload）
     *
     * @param token JWT令牌
     * @return Claims 对象，包含令牌中的所有声明
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 为指定的用户详细信息生成JWT令牌
     *
     * @param userDetails 用户详细信息，包含用户名等
     * @return 生成的JWT令牌字符串
     */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expire))
                .signWith(getKey())
                .compact();
    }

    /**
     * 验证JWT令牌是否有效且与提供的用户详细信息匹配
     *
     * @param token JWT令牌
     * @param userDetails 用户详细信息
     * @return 如果令牌有效且用户名匹配则返回true，否则返回false
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * 检查JWT令牌是否已过期
     *
     * @param token JWT令牌
     * @return 如果令牌已过期则返回true，否则返回false
     */
    private Boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
