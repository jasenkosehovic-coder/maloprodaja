package ba.maloprodaja.sifarnici.varijanta.service;

import ba.maloprodaja.sifarnici.varijanta.dto.VarijantaDTO;
import ba.maloprodaja.sifarnici.varijanta.entity.VarijantaArtikla;

import java.util.List;

public interface IVarijantaService {

    List<VarijantaDTO.ListItemDTO> listByArtikl(Long idArtikla, Long idKompanije);

    VarijantaDTO.ListItemDTO create(Long idArtikla, VarijantaDTO.CreateDTO dto, Long idKompanije);

    VarijantaDTO.ListItemDTO updateAktivan(Long id, VarijantaDTO.UpdateAktivanDTO dto, Long idKompanije);

    List<VarijantaDTO.StanjeVarijanteDTO> stanjeByArtikl(Long idArtikla, Long idKompanije);

    List<VarijantaDTO.StanjePoslovniceDTO> stanjeByVarijanta(Long idVarijante, Long idKompanije);

    VarijantaDTO.StanjePoslovniceDTO updateStanje(Long id, VarijantaDTO.UpdateStanjeDTO dto, Long idKompanije);

    VarijantaDTO.StanjePoslovniceDTO updateZalihe(Long id, VarijantaDTO.UpdateZaliheDTO dto, Long idKompanije);

    VarijantaArtikla kreirajDefaultVarijantu(Long idArtikla, Long idKompanije);

    List<VarijantaDTO.ZalihaListItemDTO> listZaliheByPoslovnica(Long idPoslovnice, Long idKompanije);
}
