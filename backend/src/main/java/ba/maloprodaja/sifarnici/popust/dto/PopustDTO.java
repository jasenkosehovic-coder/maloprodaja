package ba.maloprodaja.sifarnici.popust.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PopustDTO {

    public record ListItemDTO(
            Long id,
            String naziv,
            BigDecimal procenat,
            LocalDate datumOd,
            LocalDate datumDo,
            boolean aktivan,
            Long idPoslovnice,
            String poslovnicaNaziv
    ) {}

    public record CreateDTO(
            @NotBlank(message = "Naziv je obavezan")
            @Size(max = 200, message = "Naziv ne smije biti duži od 200 znakova")
            String naziv,

            @NotNull(message = "Procenat je obavezan")
            @DecimalMin(value = "0.00", message = "Procenat ne može biti negativan")
            BigDecimal procenat,

            LocalDate datumOd,
            LocalDate datumDo,

            Long idPoslovnice
    ) {}

    public record UpdateDTO(
            @NotBlank(message = "Naziv je obavezan")
            @Size(max = 200, message = "Naziv ne smije biti duži od 200 znakova")
            String naziv,

            @NotNull(message = "Procenat je obavezan")
            @DecimalMin(value = "0.00", message = "Procenat ne može biti negativan")
            BigDecimal procenat,

            LocalDate datumOd,
            LocalDate datumDo,

            Boolean aktivan,

            Long idPoslovnice
    ) {}
}
