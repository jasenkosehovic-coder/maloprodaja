package ba.maloprodaja.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDTO(
        @NotBlank(message = "Refresh token je obavezan")
        String refreshToken
) {}
