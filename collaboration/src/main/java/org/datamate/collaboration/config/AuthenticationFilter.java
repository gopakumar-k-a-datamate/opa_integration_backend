package org.datamate.collaboration.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Temporary Stub Filter for Epic 1 & 2 testing.
 * Automatically authenticates every incoming request to bypass 401s.
 * Uses the X-Sender-Id header as the Principal name, or defaults to "test-user".
 */
@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Use the testing header, or fallback to a dummy user
        String senderId = request.getHeader("X-Sender-Id");
        if (senderId == null || senderId.isBlank()) {
            senderId = "test-user";
        }

        // Force Spring Security to recognize this request as fully authenticated
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                senderId,
                null,
                Collections.emptyList()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
