package com.tuiop.markethub.ratelimiter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuiop.markethub.common.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Component
public class RateLimitFilter extends OncePerRequestFilter {


    private final FixedWindowRateLimiter fixedWindowRateLimiter;
    private final RateLimitProperties rateLimitProperties;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if(!rateLimitProperties.enabled()){
            filterChain.doFilter(request, response);
            return;
        }

        String client =  Optional.ofNullable(request.getRemoteAddr()).orElse("unknown");



        boolean allowed = fixedWindowRateLimiter.allowRequest(client, rateLimitProperties.requestLimit(), rateLimitProperties.windowSize());

        if(!allowed) {

            ApiError apiError = new ApiError(
                    Instant.now(),
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                    "Too many requests. Please try again later.",
                    request.getRequestURI()
            );

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Retry-After", String.valueOf(rateLimitProperties.windowSize().toSeconds()));

            objectMapper.writeValue(response.getWriter(), apiError);
            return;
        }
        filterChain.doFilter(request, response);
    }



    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        String path = request.getRequestURI();

        return  path.startsWith("/swagger-ui") || path.startsWith("/v3/api/docs") || path.startsWith("/actuator");

    }
}
