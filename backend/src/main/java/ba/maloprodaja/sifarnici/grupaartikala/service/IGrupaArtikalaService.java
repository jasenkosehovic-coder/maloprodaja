package ba.maloprodaja.sifarnici.grupaartikala.service;

import ba.maloprodaja.sifarnici.grupaartikala.dto.GrupaArtikalaDTO;

import java.util.List;

public interface IGrupaArtikalaService {

    List<GrupaArtikalaDTO.ListItemDTO> listAll(Long idKompanije);

    GrupaArtikalaDTO.ListItemDTO create(GrupaArtikalaDTO.CreateDTO dto, Long idKompanije);

    GrupaArtikalaDTO.ListItemDTO update(Long id, GrupaArtikalaDTO.UpdateDTO dto);

    void deactivate(Long id);
}
