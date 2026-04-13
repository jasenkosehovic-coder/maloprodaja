package ba.maloprodaja.sifarnici.velicina.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TipVelicinaDTO {

    public record ListItemDTO(
            Long id,
            String naziv,
            String opis,
            boolean aktivan
    ) {}

    public record CreateDTO(
            @NotBlank(message = "Naziv je obavezan")
            @Size(max = 100, message = "Naziv ne smije biti duži od 100 znakova")
            String naziv,

            @Size(max = 300, message = "Opis ne smije biti duži od 300 znakova")
            String opis
    ) {}

    public record UpdateDTO(
            @NotBlank(message = "Naziv je obavezan")
            @Size(max = 100, message = "Naziv ne smije biti duži od 100 znakova")
            String naziv,

            @Size(max = 300, message = "Opis ne smije biti duži od 300 znakova")
            String opis,

            Boolean aktivan
    ) {}
}
