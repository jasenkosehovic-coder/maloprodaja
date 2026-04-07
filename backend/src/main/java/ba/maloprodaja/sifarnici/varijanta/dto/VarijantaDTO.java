package ba.maloprodaja.sifarnici.varijanta.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class VarijantaDTO {

    public record BarkodInfo(Long id, String barkod, boolean aktivan) {}

    public record ListItemDTO(
            Long id,
            Long idArtikla,
            Long idVelicine,
            String oznakaVelicine,
            Long idBoje,
            String nazivBoje,
            String hexKodBoje,
            String nazivVarijante,
            List<BarkodInfo> barkodovi,
            boolean aktivan
    ) {}

    public record CreateDTO(
            Long idVelicine,
            Long idBoje
    ) {}

    public record UpdateAktivanDTO(
            @NotNull(message = "Polje aktivan je obavezno")
            Boolean aktivan
    ) {}

    public record StanjeVarijanteDTO(
            Long varijantaId,
            String oznakaVelicine,
            List<StanjePoslovniceDTO> poslovnice
    ) {}

    public record StanjePoslovniceDTO(
            Long idPoslovnice,
            String nazivPoslovnice,
            BigDecimal kolicina,
            BigDecimal minZaliha,
            BigDecimal optimalnaZaliha
    ) {}

    public record UpdateStanjeDTO(
            @NotNull(message = "Količina je obavezna")
            BigDecimal kolicina,
            BigDecimal minZaliha,
            BigDecimal optimalnaZaliha
    ) {}
}
