package ba.maloprodaja.sifarnici.proizvodjac.service;

import ba.maloprodaja.sifarnici.proizvodjac.dto.ProizvodjacDTO;

import java.util.List;

public interface IProizvodjacService {

    List<ProizvodjacDTO.ListItemDTO> listAll(Long idKompanije);

    ProizvodjacDTO.ListItemDTO create(ProizvodjacDTO.CreateDTO dto, Long idKompanije);

    ProizvodjacDTO.ListItemDTO update(Long id, ProizvodjacDTO.UpdateDTO dto);

    void deactivate(Long id);
}
