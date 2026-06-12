package com.wechat.ordering.util;

import io.jsonwebtoken.*;
import java.util.Date;

/**
 * JWT 工具类
 */
public class JwtUtil {

    private static final String SECRET = "wx-ordering-system-secret-key-2024";
    private static final long EXPIRE = 7 * 24 * 60 * 60 * 1000L; // 7天

    /** 生成Token */
    public static String generate(Long adminId, String username) {
        return Jwts.builder()
                .setSubject(String.valueOf(adminId))
                .claim("username", username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    /** 从Token中获取管理员ID */
    public static Long getAdminId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody();
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    /** 验证Token是否有效 */
    public static boolean validate(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
