package com.tbank.marketplace.security;

import com.tbank.marketplace.model.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        String method = request.getMethod();
        String authHeader = request.getHeader("Authorization");

        log.debug("Filtering request: {} {}", method, path);

        if (path.startsWith("/api/auth/") ||
                path.startsWith("/actuator/health") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui")) {
            log.debug("Пропускаю проверку токена для пути: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("Токен не отправлен для пути: {}", path);
            sendError(response, "UNAUTHORIZED", "Authentication required");
            return;
        }

        final String token = authHeader.substring(7);

        try {
            String email = jwtUtil.extractUsername(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.debug("Аутентификация отсутствует, проверяем токен для пользователя: {}", email);

                User userDetails = (User) userDetailsService.loadUserByUsername(email);

                if (jwtUtil.isTokenExpired(token)) {
                    log.debug("Срок действия токена истек: {}", path);
                    sendError(response, "TOKEN_EXPIRED", "Token has expired");
                    return;
                }

                if (jwtUtil.validateAccessToken(token, userDetails)) {
                    log.debug("Token valid для пользователя: {}", email);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    String userId = jwtUtil.extractUserId(token);
                    request.setAttribute("userId", userId);

                    log.debug("Установлена аутентификация для пользователя: {} (id={})", email, userId);
                } else {
                    log.debug("Токен не валиден для пользователя: {}", email);
                    sendError(response, "TOKEN_INVALID", "Invalid token");
                    return;
                }
            } else {
                if (email == null) {
                    log.warn("Не удалось извлечь email из токена");
                } else {
                    log.debug("Аутентификация уже установлена для пользователя: {}", email);
                }
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.warn("Токен истек: {}", e.getMessage());
            sendError(response, "TOKEN_EXPIRED", "Срок действия токена истек");
        } catch (MalformedJwtException e) {
            log.warn("Невалидный формат токена: {}", e.getMessage());
            sendError(response, "TOKEN_INVALID", "Невалидный формат токена");
        } catch (SignatureException e) {
            log.warn("Невалидная подпись токена: {}", e.getMessage());
            sendError(response, "TOKEN_INVALID", "Невалидная сигнатура токена");
        } catch (io.jsonwebtoken.JwtException e) {
            log.warn("JWT ошибка: {}", e.getMessage());
            sendError(response, "TOKEN_INVALID", "Токен не действительный");
        }
    }

    private void sendError(HttpServletResponse response, String errorCode, String message) throws IOException {
        if (!response.isCommitted()) {
            response.reset();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(String.format(
                    "{\"error_code\":\"%s\",\"message\":\"%s\"}",
                    errorCode, message
            ));
            response.getWriter().flush();
        }
    }
}