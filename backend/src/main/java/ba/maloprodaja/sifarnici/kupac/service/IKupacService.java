package ba.maloprodaja.sifarnici.kupac.service;

import ba.maloprodaja.sifarnici.kupac.dto.KupacDTO;

import java.util.List;

public interface IKupacService {

    List<KupacDTO.ListItemDTO> listAll(Long idKompanije);

    KupacDTO.ListItemDTO create(KupacDTO.CreateDTO dto, Long idKompanije);

    KupacDTO.ListItemDTO update(Long id, KupacDTO.UpdateDTO dto);

    void deactivate(Long id);
}
