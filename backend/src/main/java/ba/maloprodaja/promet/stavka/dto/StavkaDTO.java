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

            @NotNull(message = "VPC je obavezna")
            @DecimalMin(value = "0", message = "VPC ne može biti negativna")
            BigDecimal vpc,

            @DecimalMin(value = "0", message = "Popust ne može biti negativan")
            @DecimalMax(value = "100", message = "Popust ne može biti veći od 100")
            BigDecimal popustProcenat,

            @DecimalMin(value = "0", message = "PDV ne može biti negativan")
            @DecimalMax(value = "100", message = "PDV ne može biti veći od 100")
            BigDecimal pdvProcenat,

            @DecimalMin(value = "0", message = "Marža ne može biti negativna")
            BigDecimal marzaProcenat
    ) {
        public CreateStavkaDTO {
            if (popustProcenat == null) {
                popustProcenat = BigDecimal.ZERO;
            }
            if (pdvProcenat == null) {
                pdvProcenat = BigDecimal.ZERO;
            }
            if (marzaProcenat == null) {
                marzaProcenat = BigDecimal.ZERO;
            }
        }
    }

    public record UpdateStavkaDTO(
            @NotNull(message = "Količina je obavezna")
            @DecimalMin(value = "0.001", message = "Količina mora biti veća od 0")
            BigDecimal kolicina,

            @NotNull(message = "VPC je obavezna")
            @DecimalMin(value = "0", message = "VPC ne može biti negativna")
            BigDecimal vpc,

            @DecimalMin(value = "0", message = "Popust ne može biti negativan")
            @DecimalMax(value = "100", message = "Popust ne može biti veći od 100")
            BigDecimal popustProcenat,

            @DecimalMin(value = "0", message = "PDV ne može biti negativan")
            @DecimalMax(value = "100", message = "PDV ne može biti veći od 100")
            BigDecimal pdvProcenat,

            @DecimalMin(value = "0", message = "Marža ne može biti negativna")
            BigDecimal marzaProcenat
    ) {
        public UpdateStavkaDTO {
            if (popustProcenat == null) {
                popustProcenat = BigDecimal.ZERO;
            }
            if (pdvProcenat == null) {
                pdvProcenat = BigDecimal.ZERO;
            }
            if (marzaProcenat == null) {
                marzaProcenat = BigDecimal.ZERO;
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
            BigDecimal vpc,
            BigDecimal mpc,
            BigDecimal popustProcenat,
            BigDecimal pdvProcenat,
            BigDecimal marzaProcenat,
            BigDecimal iznosVpc,
            BigDecimal iznosMpc,
            BigDecimal iznosMarze,
            BigDecimal iznosPopusta,
            BigDecimal iznosPdv,
            LocalDateTime sysCreatedDate
    ) {}
}
