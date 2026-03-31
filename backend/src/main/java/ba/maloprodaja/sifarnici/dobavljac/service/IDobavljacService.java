package ba.maloprodaja.sifarnici.dobavljac.service;

import ba.maloprodaja.sifarnici.dobavljac.dto.DobavljacDTO;

import java.util.List;

public interface IDobavljacService {

    List<DobavljacDTO.ListItemDTO> listAll(Long idKompanije);

    DobavljacDTO.ListItemDTO create(DobavljacDTO.CreateDTO dto, Long idKompanije);

    DobavljacDTO.ListItemDTO update(Long id, DobavljacDTO.UpdateDTO dto);

    void deactivate(Long id);
}
