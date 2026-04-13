package ba.maloprodaja.dokumenti.nivelacija.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class NivelacijaDTO {

    public record ListItemDTO(
            Long id,
            String broj,
            LocalDate datum,
            String vrsta,
            Long idFakture,
            Long idOtpremnice,
            int brojStavki
    ) {}

    public record DetailDTO(
            Long id,
            String broj,
            LocalDate datum,
            String vrsta,
            Long idFakture,
            Long idOtpremnice,
            String napomena,
            BigDecimal ukupnoNivelacije,
            List<StavkaDTO> stavke
    ) {}

    public record StavkaDTO(
            Long id,
            Long idArtikla,
            String nazivArtikla,
            String sifraArtikla,
            BigDecimal kolicina,
            BigDecimal vpcStara,
            BigDecimal vpcNova,
            BigDecimal mpcStara,
            BigDecimal mpcNova,
            BigDecimal iznosNivelacije
    ) {}

    public record CreateRucnaDTO(
            @NotBlank String broj,
            @NotNull LocalDate datum,
            String napomena,
            @NotEmpty List<CreateStavkaDTO> stavke
    ) {}

    public record CreateStavkaDTO(
            @NotNull Long idArtikla,
            @NotNull @Positive BigDecimal mpcNova
    ) {}
}
