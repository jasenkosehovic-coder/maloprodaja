package ba.maloprodaja.sifarnici.artikalkomp.service;

import ba.maloprodaja.sifarnici.artikalkomp.dto.ArtikalKompDTO;

import java.util.List;

public interface IArtikalKompService {

    List<ArtikalKompDTO.ListItemDTO> listAll(Long idKompanije);

    ArtikalKompDTO.ListItemDTO create(ArtikalKompDTO.CreateDTO dto, Long idKompanije);

    ArtikalKompDTO.ListItemDTO update(Long id, ArtikalKompDTO.UpdateDTO dto);

    void deactivate(Long id);
}
