package com.example.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Populates the SecurityContext from the gateway's X-User-Name/X-User-Role headers
// (set by the gateway's AuthenticationFilter after verifying the JWT), the same
// pattern customer-service/loan-service/payment-service use — so change-password
// can read the caller's identity via Authentication without re-validating the JWT here.
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String role = request.getHeader("X-User-Role");
        String username = request.getHeader("X-User-Name");
        if (StringUtils.hasText(role)) {
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
            var authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        }
        chain.doFilter(request, response);
    }
}
