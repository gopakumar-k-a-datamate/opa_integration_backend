package org.datamate.collaboration.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Middleware filter that intercepts incoming REST requests, extracts the JWT Ticket,
 * and sets the authenticated user in the SecurityContext.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtTicketAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtVerifier jwtVerifier;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            Claims claims = jwtVerifier.verify(token);

            // Create Authentication and bind to Security Context
            TicketAuthentication authentication = new TicketAuthentication(claims);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            log.debug("Successfully authenticated user: {} for thread: {}", 
                    authentication.getSub(), authentication.getThreadId());

        } catch (JwtException e) {
            log.warn("JWT Ticket authentication failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            // We do not return 401 here directly, we let the AuthenticationEntryPoint handle it
            // so Spring Security can uniformly process the failure.
        }

        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip websocket endpoints as they are handled by HandshakeInterceptors
        return path.startsWith("/ws/");
    }
}
