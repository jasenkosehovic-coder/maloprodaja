package ba.maloprodaja.sifarnici.artikalposl.dto;

import ba.maloprodaja.sifarnici.artikalkomp.entity.TipMarze;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ArtikalPoslovnicaDTO {

    public record ListItemDTO(
            Long id,
            Long idArtikla,
            String artikalNaziv,
            String artikalSifra,
            Long idPoslovnice,
            BigDecimal vpc,
            BigDecimal marza,
            TipMarze tipMarze,
            BigDecimal mpc,
            boolean aktivan,
            BigDecimal ukupnaKolicina,
            BigDecimal popustProcenat
    ) {}

    public record CreateDTO(
            @NotNull(message = "ID artikla je obavezan")
            Long idArtikla,

            @NotNull(message = "ID poslovnice je obavezan")
            Long idPoslovnice,

            @DecimalMin(value = "0.00", message = "VPC ne može biti negativan")
            BigDecimal vpc,

            @DecimalMin(value = "0.00", message = "Marža ne može biti negativna")
            BigDecimal marza,

            TipMarze tipMarze,

            @DecimalMin(value = "0.00", message = "Popust ne može biti negativan")
            @DecimalMax(value = "100.00", message = "Popust ne može biti veći od 100%")
            BigDecimal popustProcenat
    ) {}

    public record UpdateDTO(
            @DecimalMin(value = "0.00", message = "VPC ne može biti negativan")
            BigDecimal vpc,

            @DecimalMin(value = "0.00", message = "Marža ne može biti negativna")
            BigDecimal marza,

            TipMarze tipMarze,

            @DecimalMin(value = "0.00", message = "MPC ne može biti negativan")
            BigDecimal mpc,

            Boolean aktivan,

            @DecimalMin(value = "0.00", message = "Popust ne može biti negativan")
            @DecimalMax(value = "100.00", message = "Popust ne može biti veći od 100%")
            BigDecimal popustProcenat
    ) {}

    public record BatchMpcUpdateDTO(
            @NotNull(message = "ID je obavezan")
            Long id,

            @NotNull(message = "Nova MPC je obavezna")
            @DecimalMin(value = "0.00", message = "Nova MPC ne može biti negativna")
            BigDecimal novaMpc
    ) {}

    public record BatchPopustUpdateDTO(
            @NotNull(message = "ID je obavezan") Long id,
            @NotNull(message = "Popust procenat je obavezan")
            @DecimalMin(value = "0.00", message = "Popust ne može biti negativan")
            @DecimalMax(value = "100.00", message = "Popust ne može biti veći od 100%")
            BigDecimal popustProcenat
    ) {}
}
