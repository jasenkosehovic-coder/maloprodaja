package ba.maloprodaja.sifarnici.artikalkomp.service;

import ba.maloprodaja.sifarnici.artikalkomp.dto.BarkodDTO;

import java.util.List;

public interface IBarkodService {

    List<BarkodDTO.ListItemDTO> listAll(Long idKompanije);

    List<BarkodDTO.ListItemDTO> listByVarijanta(Long idVarijante);

    BarkodDTO.ListItemDTO create(BarkodDTO.CreateDTO dto, Long idKompanije);

    BarkodDTO.ListItemDTO update(Long id, BarkodDTO.UpdateDTO dto);

    void deactivate(Long id);
}
