package ba.maloprodaja.sifarnici.artikalkomp.service;

import ba.maloprodaja.sifarnici.artikalkomp.dto.BarkodDTO;

import java.util.List;

public interface IBarkodService {

    List<BarkodDTO.ListItemDTO> listByKompanija(Long idKompanije);

    BarkodDTO.ListItemDTO create(BarkodDTO.CreateDTO dto, Long idKompanije, Long idPoslovnice);

    BarkodDTO.ListItemDTO update(Long id, BarkodDTO.UpdateDTO dto);

    void deactivate(Long id);
}
