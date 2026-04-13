package ba.maloprodaja.webshop.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class WebArtikalDTO {

    public record ListItemDTO(
            Long id,
            Long idArtikla,
            String artikalNaziv,
            String artikalSifra,
            String webNaziv,
            String webOpis,
            Boolean aktivan,
            BigDecimal mpc,
            BigDecimal popust,
            BigDecimal novaMpc,
            String metaTitle,
            String metaOpis
    ) {}

    public record CreateDTO(
            @NotNull(message = "ID artikla je obavezan")
            Long idArtikla,

            String webNaziv,
            String webOpis,
            Boolean aktivan,
            BigDecimal mpc,

            @DecimalMin(value = "0", message = "Popust ne može biti negativan")
            @DecimalMax(value = "100", message = "Popust ne može biti veći od 100")
            BigDecimal popust,

            BigDecimal novaMpc,

            @Size(max = 160, message = "Meta title ne smije biti duži od 160 znakova")
            String metaTitle,

            @Size(max = 320, message = "Meta opis ne smije biti duži od 320 znakova")
            String metaOpis
    ) {}

    public record UpdateDTO(
            String webNaziv,
            String webOpis,
            Boolean aktivan,
            BigDecimal mpc,

            @DecimalMin(value = "0", message = "Popust ne može biti negativan")
            @DecimalMax(value = "100", message = "Popust ne može biti veći od 100")
            BigDecimal popust,

            BigDecimal novaMpc,

            @Size(max = 160, message = "Meta title ne smije biti duži od 160 znakova")
            String metaTitle,

            @Size(max = 320, message = "Meta opis ne smije biti duži od 320 znakova")
            String metaOpis
    ) {}
}
