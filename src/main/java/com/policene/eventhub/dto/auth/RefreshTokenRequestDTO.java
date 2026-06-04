package com.policene.eventhub.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDTO(
        @NotBlank(message = "O refresh token não pode ser vazio.") String refreshToken
) {
}
