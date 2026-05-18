package com.example.customer.aop;

import com.example.customer.annotation.RateLimit;
import com.example.customer.exception.TooManyRequestsException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);
    private static final String LOOPBACK = "127.0.0.1";
    private static final String LOOPBACK_V6 = "0:0:0:0:0:0:0:1";

    private final RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(rateLimit)")
    public Object enforce(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {
        String ip = resolveClientIp();
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String key = "rate_limit:" + ip + ":" + sig.getDeclaringType().getSimpleName() + ":" + sig.getName();

        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, rateLimit.window(), TimeUnit.SECONDS);
        }
        log.info("[RATE_LIMIT] ip={} method={} count={}/{}", ip, sig.getName(), count, rateLimit.max());
        if (count != null && count > rateLimit.max()) {
            long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            throw new TooManyRequestsException("Too many requests. Try again in " + ttl + " second(s).");
        }
        return pjp.proceed();
    }

    private String resolveClientIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return "unknown";
        HttpServletRequest request = attrs.getRequest();
        String remoteAddr = request.getRemoteAddr();
        boolean fromTrustedProxy = LOOPBACK.equals(remoteAddr) || LOOPBACK_V6.equals(remoteAddr);
        if (fromTrustedProxy) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
        }
        return remoteAddr;
    }
}
