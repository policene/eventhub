package com.policene.eventhub.dto.auth;

public record AuthResponseDTO(
        String accessToken,
        String refreshToken
) { }
