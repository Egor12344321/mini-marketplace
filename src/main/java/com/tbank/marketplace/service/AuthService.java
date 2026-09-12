package com.tbank.marketplace.service;

import com.tbank.marketplace.exceptions.auth_exceptions.InvalidCredentialsException;
import com.tbank.marketplace.exceptions.auth_exceptions.InvalidRefreshTokenException;
import com.tbank.marketplace.exceptions.auth_exceptions.UserAlreadyExistsException;
import com.tbank.marketplace.mapper.RegisterMapper;
import com.tbank.marketplace.model.AuthResponse;
import com.tbank.marketplace.model.AuthResponseUser;
import com.tbank.marketplace.model.LoginRequest;
import com.tbank.marketplace.model.RefreshTokenRequest;
import com.tbank.marketplace.model.RegisterRequest;
import com.tbank.marketplace.model.RegisterResponse;
import com.tbank.marketplace.model.entity.User;
import com.tbank.marketplace.repository.UserRepository;
import com.tbank.marketplace.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RegisterMapper registerMapper;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public RegisterResponse register(@Valid RegisterRequest registerRequest) {

        if (userRepository.existsByEmail(registerRequest.getEmail())) throw new UserAlreadyExistsException("Пользователь с таким email уже существует");

        User user = registerMapper.mapFromRegisterReqToUserEntity(registerRequest);

        User savedUser;
        try {
            savedUser = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            log.warn("Попытка повторной регистрации email: {}", user.getEmail());
            throw new UserAlreadyExistsException("Пользователь с таким email уже существует");
        }
        log.info("Пользователь: {} (id={}) успешно создан", savedUser.getEmail(), savedUser.getId());
        return registerMapper.mapFromUserEntityToRegisterResponse(savedUser);
    }

    public AuthResponse login(@Valid LoginRequest loginRequest) {

        try {
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            );
            authenticationManager.authenticate(usernamePasswordAuthenticationToken);

        } catch (AuthenticationException e){
            log.info("Пользователь ввел неправильный пароль для входа");
            throw new InvalidCredentialsException("Неправильный логин или пароль");
        }

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() ->
                {
                    log.warn("Пользователь не найден по логину: {}", loginRequest.getEmail());
                    return new InvalidCredentialsException("Неправильный логин или пароль");
                });

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        AuthResponse authResponse = createResponseForLogin(user, accessToken, refreshToken);
        log.info("Пользователь {} (id={}) успешно вошел в систему", user.getEmail(), user.getId());
        return authResponse;
    }

    public AuthResponse updateTokens(RefreshTokenRequest refreshTokenRequest) {

        String currentRefreshToken = refreshTokenRequest.getRefreshToken();
        String username = jwtUtil.extractUsername(currentRefreshToken);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new InvalidRefreshTokenException("Невалидный токен"));

        if (!jwtUtil.validateRefreshToken(currentRefreshToken, user)){
            log.warn("Пользователь {} отправил невалидный токен", user.getUsername());
            throw new InvalidRefreshTokenException("Отправлен невалидный refresh-токен");
        }

        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        AuthResponse authResponse = createResponseFoUpdateTokens(newAccessToken, newRefreshToken);
        log.info("Пользователь {} успешно обновил токены", user.getUsername());

        return authResponse;
    }

    private AuthResponse createResponseForLogin(User user, String accessToken, String refreshToken){

        AuthResponseUser userInfo = new AuthResponseUser();
        userInfo.setId(user.getId());
        userInfo.setEmail(user.getEmail());
        userInfo.setName(user.getName());
        userInfo.setRole(user.getRole().name());

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(refreshToken);
        authResponse.setTokenType("Bearer");
        authResponse.setUser(JsonNullable.of(userInfo));

        return authResponse;
    }

    private AuthResponse createResponseFoUpdateTokens(String accessToken, String refreshToken){
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setRefreshToken(refreshToken);
        authResponse.setTokenType("Bearer");
        authResponse.setUser(JsonNullable.undefined());

        return authResponse;
    }
}
