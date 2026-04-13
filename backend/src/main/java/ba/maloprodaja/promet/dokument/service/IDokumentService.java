package ba.maloprodaja.promet.dokument.service;

import ba.maloprodaja.common.dto.PageResponseDTO;
import ba.maloprodaja.promet.dokument.dto.DokumentDTO;
import ba.maloprodaja.promet.dokument.entity.StatusDokumenta;
import ba.maloprodaja.promet.stavka.dto.StavkaDTO;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface IDokumentService {

    PageResponseDTO<DokumentDTO.DokumentListItemDTO> findAll(
            Long idKompanije,
            String tipKod,
            Long idPoslovnice,
            String status,
            LocalDate datumOd,
            LocalDate datumDo,
            Pageable pageable
    );

    DokumentDTO.DokumentResponseDTO findById(Long id, Long idKompanije);

    DokumentDTO.DokumentResponseDTO create(DokumentDTO.CreateDokumentDTO dto, Long idKompanije);

    DokumentDTO.DokumentResponseDTO update(Long id, DokumentDTO.UpdateDokumentDTO dto, Long idKompanije);

    DokumentDTO.DokumentResponseDTO potvrdi(Long id, Long idKompanije);

    DokumentDTO.DokumentResponseDTO storniraj(Long id, Long idKompanije);

    DokumentDTO.DokumentResponseDTO addStavka(Long dokumentId, StavkaDTO.CreateStavkaDTO dto, Long idKompanije);

    DokumentDTO.DokumentResponseDTO updateStavka(Long stavkaId, StavkaDTO.UpdateStavkaDTO dto, Long idKompanije);

    void deleteStavka(Long stavkaId, Long idKompanije);

    void kreirajMedjuskladisnicu(DokumentDTO.CreateMedjuskladisnicaDTO dto, Long idKompanije);
}
