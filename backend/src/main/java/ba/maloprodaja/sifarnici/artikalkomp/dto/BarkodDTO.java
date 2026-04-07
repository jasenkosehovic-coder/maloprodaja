package ba.maloprodaja.sifarnici.artikalkomp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class BarkodDTO {

    public record ListItemDTO(
            Long id,
            String barkod,
            Long idVarijante,
            String oznakaVelicine,
            boolean aktivan
    ) {}

    public record CreateDTO(
            @NotBlank(message = "Barkod je obavezan")
            @Size(max = 100, message = "Barkod ne smije biti duži od 100 znakova")
            String barkod,

            @NotNull(message = "Varijanta je obavezna")
            Long idVarijante
    ) {}

    public record UpdateDTO(
            @NotBlank(message = "Barkod je obavezan")
            @Size(max = 100, message = "Barkod ne smije biti duži od 100 znakova")
            String barkod,

            Boolean aktivan
    ) {}
}
