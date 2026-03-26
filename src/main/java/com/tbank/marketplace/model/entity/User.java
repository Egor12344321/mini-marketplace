package com.tbank.marketplace.model.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.web.servlet.handler.UserRoleAuthorizationInterceptor;

import java.util.UUID;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255, name = "email")
    private String email;

    @Column(nullable = false, name = "password")
    private String password;

    @Column(nullable = false, length = 255, name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "role")
    private UserRole role = UserRole.USER;

    public enum UserRole {
        USER, SELLER, ADMIN
    }

    public boolean isSeller() {
        return this.role == UserRole.SELLER;
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }
}
