package com.policene.eventhub.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank(message = "O nome não pode ser vazio.")
        String name,

        @NotBlank(message = "O e-mail não pode ser vazio.")
        @Email(message = "O e-mail não pode ser inválido.")
        String email,

        @NotBlank (message = "A senha não pode ser vazia.")
        @Size(min = 6, message = "A senha deve ter ao menos 6 caracteres")
        String password
) { }
