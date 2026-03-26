package com.tbank.marketplace.controller;

import com.tbank.marketplace.api.AuthApi;
import com.tbank.marketplace.model.AuthResponse;
import com.tbank.marketplace.model.LoginRequest;
import com.tbank.marketplace.model.RefreshTokenRequest;
import com.tbank.marketplace.model.RegisterRequest;
import org.springframework.http.ResponseEntity;

public class AuthController implements AuthApi {
    @Override
    public ResponseEntity<AuthResponse> login(LoginRequest loginRequest) {
        return null;
    }

    @Override
    public ResponseEntity<AuthResponse> refreshToken(RefreshTokenRequest refreshTokenRequest) {
        return null;
    }

    @Override
    public ResponseEntity<AuthResponse> register(RegisterRequest registerRequest) {
        return null;
    }
}
