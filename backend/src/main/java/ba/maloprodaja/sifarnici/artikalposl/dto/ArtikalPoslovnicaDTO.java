package ba.maloprodaja.sifarnici.artikalposl.dto;

import ba.maloprodaja.sifarnici.artikalkomp.entity.TipMarze;
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
            BigDecimal kolicina,
            BigDecimal minZaliha,
            BigDecimal optimalnaZaliha,
            boolean aktivan
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

            @DecimalMin(value = "0.00", message = "Količina ne može biti negativna")
            BigDecimal kolicina,

            @DecimalMin(value = "0.00", message = "Minimalna zaliha ne može biti negativna")
            BigDecimal minZaliha,

            @DecimalMin(value = "0.00", message = "Optimalna zaliha ne može biti negativna")
            BigDecimal optimalnaZaliha
    ) {}

    public record UpdateDTO(
            @DecimalMin(value = "0.00", message = "VPC ne može biti negativan")
            BigDecimal vpc,

            @DecimalMin(value = "0.00", message = "Marža ne može biti negativna")
            BigDecimal marza,

            TipMarze tipMarze,

            @DecimalMin(value = "0.00", message = "MPC ne može biti negativan")
            BigDecimal mpc,

            @DecimalMin(value = "0.00", message = "Količina ne može biti negativna")
            BigDecimal kolicina,

            @DecimalMin(value = "0.00", message = "Minimalna zaliha ne može biti negativna")
            BigDecimal minZaliha,

            @DecimalMin(value = "0.00", message = "Optimalna zaliha ne može biti negativna")
            BigDecimal optimalnaZaliha,

            Boolean aktivan
    ) {}
}
