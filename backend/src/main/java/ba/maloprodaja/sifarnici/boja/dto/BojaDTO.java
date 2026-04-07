package ba.maloprodaja.sifarnici.boja.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class BojaDTO {

    public record ListItemDTO(
            Long id,
            String naziv,
            String hexKod,
            boolean aktivan
    ) {}

    public record CreateDTO(
            @NotBlank(message = "Naziv je obavezan")
            @Size(max = 100, message = "Naziv ne smije biti duži od 100 znakova")
            String naziv,

            @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Hex kod mora biti u formatu #RRGGBB")
            String hexKod
    ) {}

    public record UpdateDTO(
            @NotBlank(message = "Naziv je obavezan")
            @Size(max = 100, message = "Naziv ne smije biti duži od 100 znakova")
            String naziv,

            @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Hex kod mora biti u formatu #RRGGBB")
            String hexKod,

            Boolean aktivan
    ) {}
}
