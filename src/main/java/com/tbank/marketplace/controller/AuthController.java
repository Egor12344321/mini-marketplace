package com.tbank.marketplace.controller;

import com.tbank.marketplace.api.AuthApi;
import com.tbank.marketplace.model.*;
import com.tbank.marketplace.model.AuthResponse;
import com.tbank.marketplace.model.LoginRequest;
import com.tbank.marketplace.model.RefreshTokenRequest;
import com.tbank.marketplace.model.RegisterRequest;
import com.tbank.marketplace.model.RegisterResponse;
import com.tbank.marketplace.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<AuthResponse> login(@Valid LoginRequest loginRequest) {
        log.debug("Пользователь {} пытается войти в систему", loginRequest.getEmail());

        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(authResponse);
    }

    @Override
    public ResponseEntity<AuthResponse> refreshToken(RefreshTokenRequest refreshTokenRequest) {

        AuthResponse authResponse = authService.updateTokens(refreshTokenRequest);
        return ResponseEntity.ok(authResponse);
    }

    @Override
    @PostMapping("/register")
    public ResponseEntity<com.tbank.marketplace.model.RegisterResponse> register(@Valid RegisterRequest registerRequest) {
        log.debug("Началась регистрация нового пользователя");
        RegisterResponse response = authService.register(registerRequest);
        log.debug("Зарегистрирован новый пользователь");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
