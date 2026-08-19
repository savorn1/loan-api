package com.example.auth.aop;

import com.example.auth.annotation.RateLimit;
import com.example.auth.exception.TooManyRequestsException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {

    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations;
    @Mock private ProceedingJoinPoint joinPoint;
    @Mock private MethodSignature signature;
    @Mock private RateLimit rateLimit;

    @Test
    void enforce_allowsRequestsUnderTheLimit() throws Throwable {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(any())).thenReturn(3L);
        when(joinPoint.getSignature()).thenReturn(signature);
        lenient().when(signature.getDeclaringType()).thenReturn((Class) Object.class);
        lenient().when(signature.getName()).thenReturn("login");
        when(rateLimit.max()).thenReturn(5);
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = new RateLimitAspect(redisTemplate).enforce(joinPoint, rateLimit);

        assertThat(result).isEqualTo("ok");
    }

    @Test
    void enforce_blocksRequestsOverTheLimitWithoutCallingTheMethod() throws Throwable {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(any())).thenReturn(6L);
        when(joinPoint.getSignature()).thenReturn(signature);
        lenient().when(signature.getDeclaringType()).thenReturn((Class) Object.class);
        lenient().when(signature.getName()).thenReturn("login");
        when(rateLimit.max()).thenReturn(5);
        when(redisTemplate.getExpire(any(), eq(TimeUnit.SECONDS))).thenReturn(42L);

        RateLimitAspect aspect = new RateLimitAspect(redisTemplate);

        assertThatThrownBy(() -> aspect.enforce(joinPoint, rateLimit))
                .isInstanceOf(TooManyRequestsException.class)
                .hasMessageContaining("42");
        verify(joinPoint, never()).proceed();
    }
}
