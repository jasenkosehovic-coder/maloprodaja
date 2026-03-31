package ba.maloprodaja.sifarnici.popust.service;

import ba.maloprodaja.sifarnici.popust.dto.PopustDTO;

import java.util.List;

public interface IPopustService {

    List<PopustDTO.ListItemDTO> listAll(Long idKompanije);

    PopustDTO.ListItemDTO create(PopustDTO.CreateDTO dto, Long idKompanije);

    PopustDTO.ListItemDTO update(Long id, PopustDTO.UpdateDTO dto);

    void deactivate(Long id);
}
