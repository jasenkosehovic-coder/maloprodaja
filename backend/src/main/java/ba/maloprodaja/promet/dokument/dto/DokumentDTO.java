package ba.maloprodaja.promet.dokument.dto;

import ba.maloprodaja.promet.stavka.dto.StavkaDTO;
import ba.maloprodaja.promet.tipdokumenta.dto.TipDokumentaDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DokumentDTO {

    public record CreateDokumentDTO(
            @NotNull(message = "ID tipa dokumenta je obavezan")
            Long idTipa,

            @NotNull(message = "ID poslovnice je obavezan")
            Long idPoslovnice,

            Long idDobavljaca,

            @NotNull(message = "Datum je obavezan")
            LocalDate datum,

            String napomena,

            @Valid
            List<StavkaDTO.CreateStavkaDTO> stavke
    ) {}

    public record UpdateDokumentDTO(
            Long idDobavljaca,

            @NotNull(message = "Datum je obavezan")
            LocalDate datum,

            String napomena
    ) {}

    public record CreateMedjuskladisnicaDTO(
            @NotNull(message = "ID izvorne poslovnice je obavezan")
            Long izvorPoslovnicaId,

            @NotNull(message = "ID odredišne poslovnice je obavezan")
            Long odredistePoslovnicaId,

            @NotNull(message = "Datum je obavezan")
            LocalDate datum,

            String napomena,

            @NotNull(message = "Stavke su obavezne")
            @Valid
            List<StavkaDTO.CreateStavkaDTO> stavke
    ) {}

    public record DokumentResponseDTO(
            Long id,
            TipDokumentaDTO tipDokumenta,
            Long idPoslovnice,
            String poslovnicaNaziv,
            Long idDobavljaca,
            String dobavljacNaziv,
            Long idKupca,
            String status,
            String brojDokumenta,
            LocalDate datum,
            String napomena,
            BigDecimal iznosMpc,
            BigDecimal iznosVpc,
            BigDecimal iznosPdv,
            BigDecimal iznosPopusta,
            BigDecimal iznosMarze,
            List<StavkaDTO.StavkaResponseDTO> stavke,
            LocalDateTime sysCreatedDate,
            LocalDateTime sysModifiedDate
    ) {}

    public record DokumentListItemDTO(
            Long id,
            String tipKod,
            String tipNaziv,
            Long idPoslovnice,
            String poslovnicaNaziv,
            String dobavljacNaziv,
            String status,
            String brojDokumenta,
            LocalDate datum,
            BigDecimal iznosMpc,
            LocalDateTime sysCreatedDate
    ) {}
}
