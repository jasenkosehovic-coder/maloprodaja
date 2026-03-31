package ba.maloprodaja.sifarnici.artikalposl.service;

import ba.maloprodaja.sifarnici.artikalposl.dto.ArtikalPoslovnicaDTO;

import java.util.List;

public interface IArtikalPoslovnicaService {

    List<ArtikalPoslovnicaDTO.ListItemDTO> listByPoslovnica(Long idPoslovnice);

    List<ArtikalPoslovnicaDTO.ListItemDTO> listByArtikalKompanija(Long idArtikla);

    ArtikalPoslovnicaDTO.ListItemDTO create(ArtikalPoslovnicaDTO.CreateDTO dto, Long idKompanije);

    ArtikalPoslovnicaDTO.ListItemDTO update(Long id, ArtikalPoslovnicaDTO.UpdateDTO dto);

    void deactivate(Long id);
}
