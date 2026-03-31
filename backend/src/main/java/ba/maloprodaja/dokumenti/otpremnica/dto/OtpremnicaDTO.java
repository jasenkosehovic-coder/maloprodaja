package ba.maloprodaja.dokumenti.otpremnica.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class OtpremnicaDTO {

    public record ListItemDTO(
            Long id,
            String broj,
            LocalDate datum,
            String status,
            Long idPoslovnicePosiljaoca,
            String nazivPoslovnicePosiljaoca,
            Long idPoslovnicePrimaoca,
            String nazivPoslovnicePrimaoca,
            int brojStavki
    ) {}

    public record DetailDTO(
            Long id,
            String broj,
            LocalDate datum,
            String status,
            Long idPoslovnicePosiljaoca,
            String nazivPoslovnicePosiljaoca,
            Long idPoslovnicePrimaoca,
            String nazivPoslovnicePrimaoca,
            String napomena,
            List<StavkaDTO> stavke
    ) {}

    public record StavkaDTO(
            Long id,
            Long idArtikla,
            String nazivArtikla,
            String sifraArtikla,
            BigDecimal kolicina,
            BigDecimal vpcPosiljalac,
            BigDecimal mpcPosiljalac
    ) {}

    public record CreateDTO(
            @NotNull Long idPoslovnicePrimaoca,
            @NotBlank String broj,
            @NotNull LocalDate datum,
            String napomena,
            @NotEmpty @Valid List<CreateStavkaDTO> stavke
    ) {}

    public record CreateStavkaDTO(
            @NotNull Long idArtikla,
            @NotNull @Positive BigDecimal kolicina
    ) {}

    public record PotvrdiPrijemDTO(
            String napomena
    ) {}
}
