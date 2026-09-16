package com.ncs.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类（HS256）
 */
public final class JwtUtil {

    // 演示用固定密钥；生产环境应从配置中心读取
    private static final String SECRET = "ncs-charging-platform-jwt-secret-key-2026!!";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRE_MILLIS = 7L * 24 * 3600 * 1000;

    private JwtUtil() {
    }

    public static String generate(Long userId, String phone) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("phone", phone)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRE_MILLIS))
                .signWith(KEY)
                .compact();
    }

    /**
     * 解析并校验 token，返回 userId；失败返回 null
     */
    public static Long parseUserId(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(KEY).build()
                    .parseSignedClaims(token).getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }
}
