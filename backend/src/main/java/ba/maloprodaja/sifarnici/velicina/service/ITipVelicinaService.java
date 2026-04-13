package ba.maloprodaja.sifarnici.velicina.service;

import ba.maloprodaja.sifarnici.velicina.dto.TipVelicinaDTO;
import ba.maloprodaja.sifarnici.velicina.dto.VelicinaDTO;

import java.util.List;

public interface ITipVelicinaService {

    List<TipVelicinaDTO.ListItemDTO> listAll(Long idKompanije);

    TipVelicinaDTO.ListItemDTO create(TipVelicinaDTO.CreateDTO dto, Long idKompanije);

    TipVelicinaDTO.ListItemDTO update(Long id, TipVelicinaDTO.UpdateDTO dto);

    void deactivate(Long id);

    List<VelicinaDTO.ListItemDTO> listVelicine(Long idTipa);

    VelicinaDTO.ListItemDTO addVelicina(Long idTipa, VelicinaDTO.CreateDTO dto, Long idKompanije);

    VelicinaDTO.ListItemDTO updateVelicina(Long id, VelicinaDTO.UpdateDTO dto);

    void deactivateVelicina(Long id);
}
