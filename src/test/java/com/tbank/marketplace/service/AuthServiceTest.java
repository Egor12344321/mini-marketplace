package com.tbank.marketplace.service;

import com.tbank.marketplace.exceptions.auth_exceptions.InvalidCredentialsException;
import com.tbank.marketplace.exceptions.auth_exceptions.InvalidRefreshTokenException;
import com.tbank.marketplace.exceptions.auth_exceptions.UserAlreadyExistsException;
import com.tbank.marketplace.mapper.RegisterMapper;
import com.tbank.marketplace.model.AuthResponse;
import com.tbank.marketplace.model.LoginRequest;
import com.tbank.marketplace.model.RefreshTokenRequest;
import com.tbank.marketplace.model.RegisterRequest;
import com.tbank.marketplace.model.RegisterResponse;
import com.tbank.marketplace.model.entity.User;
import com.tbank.marketplace.repository.UserRepository;
import com.tbank.marketplace.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String EMAIL = "user@example.com";
    private static final String PASSWORD = "pass123";

    @Mock
    private UserRepository userRepository;
    @Mock
    private RegisterMapper registerMapper;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_success_savesUser() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        User user = user();
        when(registerMapper.mapFromRegisterReqToUserEntity(any())).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        RegisterResponse expected = new RegisterResponse();
        when(registerMapper.mapFromUserEntityToRegisterResponse(user)).thenReturn(expected);

        RegisterResponse result = authService.register(registerRequest());

        assertThat(result).isSameAs(expected);
        verify(userRepository).save(user);
    }

    @Test
    void register_duplicateEmail_throwsUserAlreadyExists() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest()))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_uniqueViolationRace_throwsUserAlreadyExists() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(registerMapper.mapFromRegisterReqToUserEntity(any())).thenReturn(user());
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> authService.register(registerRequest()))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void login_success_returnsTokensAndUser() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        User user = user();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(user)).thenReturn("accessTok");
        when(jwtUtil.generateRefreshToken(user)).thenReturn("refreshTok");

        AuthResponse response = authService.login(loginRequest());

        assertThat(response.getAccessToken()).isEqualTo("accessTok");
        assertThat(response.getRefreshToken()).isEqualTo("refreshTok");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser().isPresent()).isTrue();
        assertThat(response.getUser().get().getEmail()).isEqualTo(EMAIL);
    }

    @Test
    void login_wrongPassword_throwsInvalidCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(loginRequest()))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void login_userNotFound_throwsInvalidCredentials() {
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest()))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refresh_success_returnsNewTokens() {
        when(jwtUtil.extractUsername("refreshTok")).thenReturn(EMAIL);
        User user = user();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(jwtUtil.validateRefreshToken("refreshTok", user)).thenReturn(true);
        when(jwtUtil.generateAccessToken(user)).thenReturn("newAccess");
        when(jwtUtil.generateRefreshToken(user)).thenReturn("newRefresh");

        AuthResponse response = authService.updateTokens(refreshRequest());

        assertThat(response.getAccessToken()).isEqualTo("newAccess");
        assertThat(response.getRefreshToken()).isEqualTo("newRefresh");
    }

    @Test
    void refresh_invalidToken_throwsInvalidRefreshToken() {
        when(jwtUtil.extractUsername("refreshTok")).thenReturn(EMAIL);
        User user = user();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(jwtUtil.validateRefreshToken("refreshTok", user)).thenReturn(false);

        assertThatThrownBy(() -> authService.updateTokens(refreshRequest()))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(jwtUtil, never()).generateAccessToken(any());
    }

    @Test
    void refresh_userNotFound_throwsInvalidRefreshToken() {
        when(jwtUtil.extractUsername("refreshTok")).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.updateTokens(refreshRequest()))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    private User user() {
        return User.builder()
                .id(UUID.randomUUID())
                .email(EMAIL)
                .password(PASSWORD)
                .name("Дима")
                .role(User.UserRole.USER)
                .enabled(true)
                .accountNotExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
    }

    private RegisterRequest registerRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        request.setName("Дима");
        return request;
    }

    private LoginRequest loginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail(EMAIL);
        request.setPassword(PASSWORD);
        return request;
    }

    private RefreshTokenRequest refreshRequest() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refreshTok");
        return request;
    }
}