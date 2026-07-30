package com.example.auth.redis;

import com.example.auth.dto.JwtUserClaims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

// Additive permission cache, keyed by SysUser.uuid — NOT the source of truth for
// authentication (the base `role` claim in the JWT still drives hasRole('ADMIN')
// and the gateway's X-User-Role header, unchanged). Nothing reads this yet except
// GET /api/auth/profile; it exists so a future permission-based authorization layer
// has somewhere to read from without re-querying role_permission/user_role on
// every request.
@Component
@RequiredArgsConstructor
public class UserSessionStore {

    private static final String KEY_PREFIX = "session:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${redis.session.expire-hours:24}")
    private long expireHours;

    public void save(JwtUserClaims claims) {
        redisTemplate.opsForValue().set(KEY_PREFIX + claims.getUuid(), claims, expireHours, TimeUnit.HOURS);
    }

    public JwtUserClaims get(String uuid) {
        Object value = redisTemplate.opsForValue().get(KEY_PREFIX + uuid);
        return value instanceof JwtUserClaims claims ? claims : null;
    }

    public void delete(String uuid) {
        redisTemplate.delete(KEY_PREFIX + uuid);
    }

    public void deleteAll() {
        var keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
