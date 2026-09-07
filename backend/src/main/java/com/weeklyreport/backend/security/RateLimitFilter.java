package com.weeklyreport.backend.security;

import com.weeklyreport.backend.dto.ErrorResponse;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

/**
 * Per-IP rate limiting for the three unauthenticated auth endpoints — the only ones an anonymous
 * caller can reach and hammer. Everything else needs a valid JWT first, which already bounds
 * abuse to authenticated users, so this filter lets all other paths straight through.
 *
 * <p>Runs before {@link JwtAuthenticationFilter} so an abusive burst is rejected before the app
 * does any real work.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Duration REFILL_WINDOW = Duration.ofSeconds(60);
    private static final String RETRY_AFTER_SECONDS = String.valueOf(REFILL_WINDOW.toSeconds());

    private static final int LOGIN_ATTEMPTS_PER_WINDOW = 10;
    private static final int REGISTER_ATTEMPTS_PER_WINDOW = 5;
    private static final int FORGOT_PASSWORD_ATTEMPTS_PER_WINDOW = 5;

    private static final Map<String, Integer> LIMIT_BY_PATH = Map.of(
            "/auth/login", LOGIN_ATTEMPTS_PER_WINDOW,
            "/auth/register", REGISTER_ATTEMPTS_PER_WINDOW,
            "/auth/forgot-password", FORGOT_PASSWORD_ATTEMPTS_PER_WINDOW);

    private static final String TOO_MANY_REQUESTS_MESSAGE =
            "Too many requests. Please wait a moment before trying again.";

    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";

    /** Buckets are created on first hit and keyed by "<path>|<ip>" so each endpoint limits separately. */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    private final boolean enabled;

    public RateLimitFilter(ObjectMapper objectMapper, boolean enabled) {
        this.objectMapper = objectMapper;
        this.enabled = enabled;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Integer limit = limitFor(request);
        if (!enabled || limit == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String bucketKey = request.getRequestURI() + "|" + clientIp(request);
        Bucket bucket = buckets.computeIfAbsent(bucketKey, key -> newBucket(limit));

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            writeTooManyRequests(response);
        }
    }

    private static Integer limitFor(HttpServletRequest request) {
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return null;
        }
        return LIMIT_BY_PATH.get(request.getRequestURI());
    }

    private static Bucket newBucket(int limit) {
        return Bucket.builder()
                .addLimit(bandwidth -> bandwidth.capacity(limit).refillGreedy(limit, REFILL_WINDOW))
                .build();
    }

    private static String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader(FORWARDED_FOR_HEADER);
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",", 2)[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(HttpHeaders.RETRY_AFTER, RETRY_AFTER_SECONDS);
        objectMapper.writeValue(response.getWriter(), ErrorResponse.of(TOO_MANY_REQUESTS_MESSAGE));
    }
}
