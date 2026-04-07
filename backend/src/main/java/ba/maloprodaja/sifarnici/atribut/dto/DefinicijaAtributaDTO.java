package ba.maloprodaja.sifarnici.atribut.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class DefinicijaAtributaDTO {

    public record ListItemDTO(
            Long id,
            String naziv,
            int redosljed,
            boolean obavezno,
            boolean zaWeb,
            boolean aktivan
    ) {}

    public record CreateDTO(
            @NotBlank(message = "Naziv je obavezan")
            @Size(max = 100, message = "Naziv ne smije biti duži od 100 znakova")
            String naziv,

            @NotNull(message = "Redosljed je obavezan")
            @Min(value = 0, message = "Redosljed ne može biti negativan")
            Integer redosljed,

            @NotNull(message = "Obavezno polje je obavezno")
            Boolean obavezno,

            @NotNull(message = "Za web polje je obavezno")
            Boolean zaWeb
    ) {}

    public record UpdateDTO(
            @NotBlank(message = "Naziv je obavezan")
            @Size(max = 100, message = "Naziv ne smije biti duži od 100 znakova")
            String naziv,

            @NotNull(message = "Redosljed je obavezan")
            @Min(value = 0, message = "Redosljed ne može biti negativan")
            Integer redosljed,

            @NotNull(message = "Obavezno polje je obavezno")
            Boolean obavezno,

            @NotNull(message = "Za web polje je obavezno")
            Boolean zaWeb,

            Boolean aktivan
    ) {}
}
