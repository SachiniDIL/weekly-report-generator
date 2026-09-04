package com.weeklyreport.backend.security;

import com.weeklyreport.backend.domain.User;
import com.weeklyreport.backend.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authenticates requests that carry a valid {@code Authorization: Bearer <jwt>} header.
 *
 * <p>Every failure mode — no header, an unparseable or expired token, an unknown user, or a
 * {@code tokenVersion} that no longer matches the stored value (the token predates a password
 * reset) — leaves the request unauthenticated and lets the authorization rules reject it,
 * exactly as if no token had been supplied.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        authenticate(request)
                .ifPresent(authentication ->
                        SecurityContextHolder.getContext().setAuthentication(authentication));
        filterChain.doFilter(request, response);
    }

    private Optional<UsernamePasswordAuthenticationToken> authenticate(HttpServletRequest request) {
        String token = bearerToken(request);
        if (token == null) {
            return Optional.empty();
        }

        Claims claims;
        try {
            claims = jwtService.parseToken(token);
        } catch (InvalidTokenException e) {
            return Optional.empty();
        }

        int tokenVersion = jwtService.extractTokenVersion(claims);
        return userRepository
                .findById(jwtService.extractUserId(claims))
                .filter(user -> user.getTokenVersion() == tokenVersion)
                .filter(user -> user.getRole() != null)
                .map(JwtAuthenticationFilter::authenticationFor);
    }

    private static UsernamePasswordAuthenticationToken authenticationFor(User user) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        return new UsernamePasswordAuthenticationToken(user, null, List.of(authority));
    }

    private static String bearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return header.substring(BEARER_PREFIX.length());
    }
}
