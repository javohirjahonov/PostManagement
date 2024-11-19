package org.example.postproject.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.example.postproject.service.authentication.AuthenticationService;
import org.example.postproject.service.jwt.JwtService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class JwtFilterToken extends OncePerRequestFilter {
    private AuthenticationService authenticationService;
    private JwtService jwtService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                Claims claims = jwtService.extractToken(token).getBody();
                String username = claims.getSubject(); // Extract subject (username/email)

                if (username == null || username.isBlank()) {
                    logger.error("JWT Token is missing the subject");
                    throw new RuntimeException("Invalid JWT Token: Missing subject");
                }

                // Extract roles and handle missing or empty roles gracefully
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                Object rolesClaim = claims.get("roles");
                if (rolesClaim != null) {
                    authorities = ((List<?>) rolesClaim).stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());
                } else {
                    logger.warn("JWT Token does not contain roles");
                }

                // Set SecurityContext
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.info("Authenticated user: " + username);
            } catch (Exception e) {
                logger.error("Invalid JWT Token: " + e.getMessage(), e);
            }
        }

        chain.doFilter(request, response);
    }
}
