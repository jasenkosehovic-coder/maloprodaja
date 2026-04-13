package ba.maloprodaja.promet.stavka.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StavkaDTO {

    public record CreateStavkaDTO(
            @NotNull(message = "ID varijante je obavezan")
            Long idVarijante,

            @NotNull(message = "Količina je obavezna")
            @DecimalMin(value = "0.001", message = "Količina mora biti veća od 0")
            BigDecimal kolicina,

            @NotNull(message = "Cijena je obavezna")
            @DecimalMin(value = "0", message = "Cijena ne može biti negativna")
            BigDecimal cijena,

            @DecimalMin(value = "0", message = "Popust ne može biti negativan")
            @DecimalMax(value = "100", message = "Popust ne može biti veći od 100")
            BigDecimal popust
    ) {
        public CreateStavkaDTO {
            if (popust == null) {
                popust = BigDecimal.ZERO;
            }
        }
    }

    public record UpdateStavkaDTO(
            @NotNull(message = "Količina je obavezna")
            @DecimalMin(value = "0.001", message = "Količina mora biti veća od 0")
            BigDecimal kolicina,

            @NotNull(message = "Cijena je obavezna")
            @DecimalMin(value = "0", message = "Cijena ne može biti negativna")
            BigDecimal cijena,

            @DecimalMin(value = "0", message = "Popust ne može biti negativan")
            @DecimalMax(value = "100", message = "Popust ne može biti veći od 100")
            BigDecimal popust
    ) {
        public UpdateStavkaDTO {
            if (popust == null) {
                popust = BigDecimal.ZERO;
            }
        }
    }

    public record StavkaResponseDTO(
            Long id,
            Long idVarijante,
            String artikalNaziv,
            String velicinaOznaka,
            String bojaNaziv,
            BigDecimal kolicina,
            BigDecimal cijena,
            BigDecimal popust,
            BigDecimal ukupno,
            LocalDateTime sysCreatedDate
    ) {}
}
