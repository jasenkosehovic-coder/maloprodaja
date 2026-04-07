package ba.maloprodaja.sifarnici.slika.service;

import ba.maloprodaja.sifarnici.slika.dto.SlikaArtiklaDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ISlikaArtiklaService {

    List<SlikaArtiklaDTO.ListItemDTO> listSlike(Long idArtikla);

    SlikaArtiklaDTO.ListItemDTO uploadSlika(Long idArtikla, MultipartFile file, Long idKompanije);

    SlikaArtiklaDTO.ListItemDTO update(Long id, SlikaArtiklaDTO.UpdateDTO dto);

    void deactivate(Long id);
}
