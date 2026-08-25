package com.example.loan.aop;

import com.example.loan.annotation.RateLimit;
import com.example.loan.exception.TooManyRequestsException;
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);

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
        if (isTrustedProxy(remoteAddr)) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
        }
        return remoteAddr;
    }

    // Loopback covers a reverse proxy on the same host; site-local (RFC1918: 10/8,
    // 172.16/12, 192.168/16 — Docker's default bridge networks live in this range)
    // covers api-gateway calling in from another container on docker-compose's
    // internal loan-net, which in docker-compose.prod.yml is the only way to reach
    // this service at all (no ports: published for it there). A direct public-internet
    // caller has a public remoteAddr either way, so their self-reported X-Forwarded-For
    // is never trusted. Advisory rate limiting only, not a hard security boundary — see
    // RateLimit.
    private boolean isTrustedProxy(String remoteAddr) {
        try {
            InetAddress addr = InetAddress.getByName(remoteAddr);
            return addr.isLoopbackAddress() || addr.isSiteLocalAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
