package com.clickkart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType;
    private Long userId;
    private String name;
    private String email;
    private String role;

    public static AuthResponse of(String token, Long userId, String name, String email, String role) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(userId)
                .name(name)
                .email(email)
                .role(role)
                .build();
    }
}
