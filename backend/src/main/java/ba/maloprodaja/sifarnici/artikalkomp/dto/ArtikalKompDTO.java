package ba.maloprodaja.sifarnici.artikalkomp.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Map;

public class ArtikalKompDTO {

    public record ListItemDTO(
            Long id,
            String naziv,
            String sifra,
            String opis,
            String jedin,
            BigDecimal pdv,
            boolean aktivan,
            Long idGrupe,
            String nazivGrupe,
            Long idProizvodjaca,
            String nazivProizvodjaca,
            Long idDobavljaca,
            String nazivDobavljaca,
            Long idTipaVelicina,
            String nazivTipaVelicina,
            Map<Long, Long> atributi,
            BigDecimal popustProcenat
    ) {}

    public record CreateDTO(
            @NotBlank(message = "Naziv je obavezan")
            @Size(max = 300, message = "Naziv ne smije biti duži od 300 znakova")
            String naziv,

            @NotBlank(message = "Šifra je obavezna")
            @Size(max = 100, message = "Šifra ne smije biti duža od 100 znakova")
            String sifra,

            @Size(max = 1000, message = "Opis ne smije biti duži od 1000 znakova")
            String opis,

            @Size(max = 20, message = "Jedinica mjere ne smije biti duža od 20 znakova")
            String jedin,

            @NotNull(message = "PDV je obavezan")
            @DecimalMin(value = "0.00", message = "PDV ne može biti negativan")
            BigDecimal pdv,

            Long idGrupe,
            Long idProizvodjaca,
            Long idDobavljaca,
            Long idTipaVelicina
    ) {}

    public record UpdateDTO(
            @NotBlank(message = "Naziv je obavezan")
            @Size(max = 300, message = "Naziv ne smije biti duži od 300 znakova")
            String naziv,

            @NotBlank(message = "Šifra je obavezna")
            @Size(max = 100, message = "Šifra ne smije biti duža od 100 znakova")
            String sifra,

            @Size(max = 1000, message = "Opis ne smije biti duži od 1000 znakova")
            String opis,

            @Size(max = 20, message = "Jedinica mjere ne smije biti duža od 20 znakova")
            String jedin,

            @NotNull(message = "PDV je obavezan")
            @DecimalMin(value = "0.00", message = "PDV ne može biti negativan")
            BigDecimal pdv,

            Long idGrupe,
            Long idProizvodjaca,
            Long idDobavljaca,
            Long idTipaVelicina,

            Boolean aktivan,

            @DecimalMin(value = "0.00", message = "Popust ne može biti negativan")
            @DecimalMax(value = "100.00", message = "Popust ne može biti veći od 100%")
            BigDecimal popustProcenat
    ) {}
}
