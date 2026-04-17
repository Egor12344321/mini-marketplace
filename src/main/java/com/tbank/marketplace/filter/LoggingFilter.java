package com.tbank.marketplace.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbank.marketplace.model.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString();
        response.setHeader("X-Request-Id", requestId);

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, 10000);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            Map<String, Object> logEntry = new LinkedHashMap<>();
            logEntry.put("request_id", requestId);
            logEntry.put("method", request.getMethod());
            logEntry.put("endpoint", request.getRequestURI());
            logEntry.put("status_code", response.getStatus());
            logEntry.put("duration_ms", duration);
            logEntry.put("user_id", getUserId());
            logEntry.put("timestamp", Instant.now().toString());

            String method = request.getMethod();
            if (method.equals("POST") || method.equals("PUT") || method.equals("DELETE") || method.equals("PATCH")) {
                String body = getRequestBody(requestWrapper);
                if (body != null && !body.isEmpty()) {
                    logEntry.put("request_body", maskSensitive(body));
                }
            }

            log.info(objectMapper.writeValueAsString(logEntry));

            responseWrapper.copyBodyToResponse();
        }
    }

    private String getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return ((User) auth.getPrincipal()).getId().toString();
        }
        return null;
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) return null;
        try {
            return new String(content, request.getCharacterEncoding());
        } catch (Exception e) {
            return null;
        }
    }

    private Object parseJsonBody(String body) {
        if (body == null || body.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(body, Object.class);
        } catch (Exception e) {
            return body;
        }
    }


    private String maskSensitive(String body) {
        if (body == null) return null;
        body = body.replaceAll("(?i)\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"");
        body = body.replaceAll("(?i)\"refresh_token\"\\s*:\\s*\"[^\"]*\"", "\"refresh_token\":\"***\"");
        return body;
    }
}