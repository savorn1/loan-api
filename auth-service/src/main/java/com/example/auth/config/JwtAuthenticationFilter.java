package com.example.auth.config;

import com.example.auth.dto.JwtUserClaims;
import com.example.auth.redis.UserSessionStore;
import com.example.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Verifies the JWT itself (same defense-in-depth this service's downstream siblings
// already have — see customer/loan/payment-service's own JwtAuthenticationFilter),
// then additionally looks up the caller's cached permission list in Redis (see
// UserSessionStore) and adds each permission name as its own GrantedAuthority. This
// is what lets @PreAuthorize("hasAuthority('USER_READ')") work, alongside the
// existing ROLE_<role> authority that hasRole('ADMIN') checks still rely on — a
// missing/expired Redis session (e.g. after logout) just means no permission
// authorities get added, not that the request is rejected outright.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserSessionStore userSessionStore;

    public JwtAuthenticationFilter(JwtService jwtService, UserSessionStore userSessionStore) {
        this.jwtService = jwtService;
        this.userSessionStore = userSessionStore;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.isValid(token)) {
                String username = jwtService.extractUsername(token);
                String role = jwtService.extractRole(token);
                String uuid = jwtService.extractUuid(token);

                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

                if (uuid != null) {
                    JwtUserClaims claims = userSessionStore.get(uuid);
                    if (claims != null && claims.getPermissions() != null) {
                        claims.getPermissions().forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
                    }
                }

                var authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            }
        }
        chain.doFilter(request, response);
    }
}
