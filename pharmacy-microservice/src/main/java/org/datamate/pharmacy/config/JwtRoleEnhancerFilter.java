package org.datamate.pharmacy.config;

import com.datamate.bedrock.framework.common.security.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtRoleEnhancerFilter extends OncePerRequestFilter {

    private final SecretKey key;

    public JwtRoleEnhancerFilter(SecurityProperties securityProperties) {
        this.key = Keys.hmacShaKeyFor(securityProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);
                Claims claims = (Claims) Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                List<String> roles = null;
                if (claims.containsKey("roles")) {
                    roles = claims.get("roles", List.class);
                } else if (claims.containsKey("role")) {
                    Object roleClaim = claims.get("role");
                    if (roleClaim instanceof List) {
                        roles = (List<String>) roleClaim;
                    } else if (roleClaim instanceof String) {
                        roles = List.of((String) roleClaim);
                    }
                }

                if (roles != null) {
                    List<GrantedAuthority> authorities = roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());

                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    Object principal = (auth != null) ? auth.getPrincipal() : claims.getSubject();
                    Object credentials = (auth != null) ? auth.getCredentials() : null;

                    UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                            principal, credentials, authorities);
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                }
            } catch (Exception e) {
                // Ignore and proceed
            }
        }
        filterChain.doFilter(request, response);
    }
}
