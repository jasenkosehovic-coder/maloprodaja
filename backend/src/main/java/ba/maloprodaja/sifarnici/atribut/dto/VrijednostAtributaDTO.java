package ba.maloprodaja.sifarnici.atribut.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class VrijednostAtributaDTO {

    public record ListItemDTO(
            Long id,
            Long idDefinicije,
            String vrijednost,
            int redosljed,
            boolean aktivan
    ) {}

    public record CreateDTO(
            @NotBlank(message = "Vrijednost je obavezna")
            @Size(max = 200, message = "Vrijednost ne smije biti duža od 200 znakova")
            String vrijednost,

            @NotNull(message = "Redosljed je obavezan")
            @Min(value = 0, message = "Redosljed ne može biti negativan")
            Integer redosljed
    ) {}

    public record UpdateDTO(
            @NotBlank(message = "Vrijednost je obavezna")
            @Size(max = 200, message = "Vrijednost ne smije biti duža od 200 znakova")
            String vrijednost,

            @NotNull(message = "Redosljed je obavezan")
            @Min(value = 0, message = "Redosljed ne može biti negativan")
            Integer redosljed,

            Boolean aktivan
    ) {}
}
