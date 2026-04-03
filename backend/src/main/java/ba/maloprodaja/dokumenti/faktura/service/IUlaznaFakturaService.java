package ba.maloprodaja.dokumenti.faktura.service;

import ba.maloprodaja.dokumenti.faktura.dto.UlaznaFakturaDTO;

import java.util.List;

public interface IUlaznaFakturaService {

    List<UlaznaFakturaDTO.ListItemDTO> listAll(Long idKompanije, Long idPoslovnice, Integer godina);

    UlaznaFakturaDTO.DetailDTO findById(Long id);

    UlaznaFakturaDTO.DetailDTO create(UlaznaFakturaDTO.CreateDTO dto, Long idKompanije, Long idPoslovnice);

    UlaznaFakturaDTO.DetailDTO update(Long id, UlaznaFakturaDTO.UpdateDTO dto);

    UlaznaFakturaDTO.DetailDTO addStavka(Long fakturaId, UlaznaFakturaDTO.AddStavkaDTO dto);

    UlaznaFakturaDTO.DetailDTO removeStavka(Long fakturaId, Long stavkaId);

    void potvrdi(Long id);

    void storno(Long id);

    void delete(Long id);
}
