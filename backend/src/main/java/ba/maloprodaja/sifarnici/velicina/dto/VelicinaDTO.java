package ba.maloprodaja.sifarnici.velicina.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VelicinaDTO {

    public record ListItemDTO(
            Long id,
            Long idTipaVelicina,
            String oznaka,
            int redosljed,
            boolean aktivan
    ) {}

    public record CreateDTO(
            @NotBlank(message = "Oznaka je obavezna")
            @Size(max = 20, message = "Oznaka ne smije biti duža od 20 znakova")
            String oznaka,

            @NotNull(message = "Redosljed je obavezan")
            @Min(value = 0, message = "Redosljed ne može biti negativan")
            Integer redosljed
    ) {}

    public record UpdateDTO(
            @NotBlank(message = "Oznaka je obavezna")
            @Size(max = 20, message = "Oznaka ne smije biti duža od 20 znakova")
            String oznaka,

            @NotNull(message = "Redosljed je obavezan")
            @Min(value = 0, message = "Redosljed ne može biti negativan")
            Integer redosljed,

            Boolean aktivan
    ) {}
}
