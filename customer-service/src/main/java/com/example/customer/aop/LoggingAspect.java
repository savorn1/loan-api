package com.example.customer.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.example.customer.controller..*(..)) || " +
            "execution(* com.example.customer.service.impl..*(..)) ")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        String className = sig.getDeclaringType().getSimpleName();
        String methodName = sig.getName();
        Object[] args = joinPoint.getArgs();

        log.info("[{}] {}.{}() args={}", layer(className), className, methodName, Arrays.toString(args));

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long elapsed = System.currentTimeMillis() - start;

        log.info("[{}] {}.{}() completed in {}ms", layer(className), className, methodName, elapsed);
        return result;
    }

    @AfterThrowing(
            pointcut = "execution(* com.example.customer.controller..*(..)) || " +
                       "execution(* com.example.customer.service.impl..*(..)) ",
            throwing = "ex"
    )
    public void logException(org.aspectj.lang.JoinPoint joinPoint, Throwable ex) {
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        log.error("[{}] {}.{}() threw {}: {}",
                layer(sig.getDeclaringType().getSimpleName()),
                sig.getDeclaringType().getSimpleName(),
                sig.getName(),
                ex.getClass().getSimpleName(),
                ex.getMessage());
    }

    private static String layer(String className) {
        if (className.endsWith("Controller")) return "CONTROLLER";
        if (className.endsWith("ServiceImpl")) return "SERVICE";
        return "APP";
    }
}
