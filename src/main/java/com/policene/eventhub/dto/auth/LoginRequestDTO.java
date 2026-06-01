package com.policene.eventhub.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank (message = "O e-mail não pode ser vazio.")
        @Email (message = "O e-mail não pode ser inválido.")
        String email,

        @NotBlank (message = "A senha não pode ser vazia.")
        String password) {
}
