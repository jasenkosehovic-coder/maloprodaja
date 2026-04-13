package ba.maloprodaja.sifarnici.slika.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class SlikaArtiklaDTO {

    public record ListItemDTO(
            Long id,
            Long idArtikla,
            String putanja,
            int redosljed,
            boolean jeNaslovna,
            boolean aktivan
    ) {}

    public record UpdateDTO(
            @NotNull(message = "Redosljed je obavezan")
            @Min(value = 0, message = "Redosljed ne može biti negativan")
            Integer redosljed,

            @NotNull(message = "Je naslovna je obavezno")
            Boolean jeNaslovna,

            Boolean aktivan
    ) {}

    public record ReorderItemDTO(
            @NotNull(message = "ID slike je obavezan")
            Long id,

            @NotNull(message = "Redosljed je obavezan")
            @Min(value = 0, message = "Redosljed ne može biti negativan")
            Integer redosljed
    ) {}
}
