package ba.maloprodaja.dokumenti.faktura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class UlaznaFakturaDTO {

    public record ListItemDTO(
            Long id,
            String broj,
            LocalDate datum,
            LocalDate datumValute,
            String statusFakture,
            String nazivDobavljaca,
            BigDecimal ukupnoBezPdv,
            BigDecimal ukupnoPdv,
            BigDecimal ukupno
    ) {}

    public record DetailDTO(
            Long id,
            String broj,
            LocalDate datum,
            LocalDate datumValute,
            String statusFakture,
            Long idDobavljaca,
            String nazivDobavljaca,
            BigDecimal ukupnoBezPdv,
            BigDecimal ukupnoPdv,
            BigDecimal ukupno,
            String napomena,
            List<StavkaDTO> stavke
    ) {}

    public record StavkaDTO(
            Long id,
            Long idArtikla,
            String nazivArtikla,
            String sifraArtikla,
            BigDecimal kolicina,
            BigDecimal vpc,
            BigDecimal pdvStopa,
            BigDecimal iznosPdv,
            BigDecimal ukupno
    ) {}

    public record CreateDTO(
            @NotNull Long idDobavljaca,
            @NotBlank String broj,
            @NotNull LocalDate datum,
            LocalDate datumValute,
            String napomena,
            BigDecimal ukupnoBezPdv,
            BigDecimal ukupno
    ) {}

    public record CreateStavkaDTO(
            @NotNull Long idArtikla,
            @NotNull @Positive BigDecimal kolicina,
            @NotNull @Positive BigDecimal vpc,
            @NotNull BigDecimal pdvStopa
    ) {}

    public record AddStavkaDTO(
            @NotNull Long idArtikla,
            @NotNull @Positive BigDecimal kolicina,
            @NotNull @Positive BigDecimal vpc,
            @NotNull BigDecimal pdvStopa
    ) {}

    public record UpdateDTO(
            Long idDobavljaca,
            String broj,
            LocalDate datum,
            LocalDate datumValute,
            String napomena
    ) {}
}
