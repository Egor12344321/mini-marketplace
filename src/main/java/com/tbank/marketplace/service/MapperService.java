package com.tbank.marketplace.service;

import com.tbank.marketplace.model.RegisterRequest;
import com.tbank.marketplace.model.RegisterResponse;
import com.tbank.marketplace.model.RegisterResponseUser;
import com.tbank.marketplace.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MapperService {

    private final PasswordEncoder encoder;

    public User mapFromRegisterReqToUserEntity(RegisterRequest registerRequest) {
        return User.builder()
                .email(registerRequest.getEmail())
                .password(encoder.encode(registerRequest.getPassword()))
                .name(registerRequest.getName())
                .role(User.UserRole.USER)
                .enabled(true)
                .accountNotExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
    }

    public RegisterResponse mapFromUserEntityToRegisterResponse(User user) {
        RegisterResponseUser responseUser = new RegisterResponseUser();
        responseUser.setId(user.getId());
        responseUser.setEmail(user.getEmail());
        responseUser.setName(user.getName());

        String role = user.getRole().name();
        responseUser.setRole(RegisterResponseUser.RoleEnum.fromValue(role));

        if (user.getCreatedAt() != null) {
            responseUser.setCreatedAt(user.getCreatedAt().atOffset(ZoneOffset.UTC));
        }

        RegisterResponse response = new RegisterResponse();
        response.setMessage("Пользователь успешно зарегистрирован");
        response.setUser(responseUser);

        return response;
    }
}