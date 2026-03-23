package ba.maloprodaja.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank(message = "Korisničko ime je obavezno")
        String username,

        @NotBlank(message = "Lozinka je obavezna")
        String password
) {}
