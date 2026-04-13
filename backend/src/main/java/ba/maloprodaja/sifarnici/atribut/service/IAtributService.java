package ba.maloprodaja.sifarnici.atribut.service;

import ba.maloprodaja.sifarnici.atribut.dto.ArtikalAtributDTO;
import ba.maloprodaja.sifarnici.atribut.dto.DefinicijaAtributaDTO;
import ba.maloprodaja.sifarnici.atribut.dto.VrijednostAtributaDTO;

import java.util.List;

public interface IAtributService {

    List<DefinicijaAtributaDTO.ListItemDTO> listDefinicije(Long idKompanije);

    DefinicijaAtributaDTO.ListItemDTO createDefinicija(DefinicijaAtributaDTO.CreateDTO dto, Long idKompanije);

    DefinicijaAtributaDTO.ListItemDTO updateDefinicija(Long id, DefinicijaAtributaDTO.UpdateDTO dto);

    void deactivateDefinicija(Long id);

    List<VrijednostAtributaDTO.ListItemDTO> listVrijednosti(Long idDefinicije);

    VrijednostAtributaDTO.ListItemDTO addVrijednost(Long idDefinicije, VrijednostAtributaDTO.CreateDTO dto, Long idKompanije);

    VrijednostAtributaDTO.ListItemDTO updateVrijednost(Long id, VrijednostAtributaDTO.UpdateDTO dto);

    void deactivateVrijednost(Long id);

    List<ArtikalAtributDTO.ListItemDTO> listArtikalAtributi(Long idArtikla);

    List<ArtikalAtributDTO.ListItemDTO> upsertArtikalAtributi(Long idArtikla, List<ArtikalAtributDTO.UpsertItemDTO> dto, Long idKompanije);
}
