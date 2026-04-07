package ba.maloprodaja.sifarnici.boja.service;

import ba.maloprodaja.sifarnici.boja.dto.BojaDTO;

import java.util.List;

public interface IBojaService {

    List<BojaDTO.ListItemDTO> listAll(Long idKompanije);

    List<BojaDTO.ListItemDTO> listAktivne(Long idKompanije);

    BojaDTO.ListItemDTO create(BojaDTO.CreateDTO dto, Long idKompanije);

    BojaDTO.ListItemDTO update(Long id, BojaDTO.UpdateDTO dto, Long idKompanije);

    void delete(Long id, Long idKompanije);
}
