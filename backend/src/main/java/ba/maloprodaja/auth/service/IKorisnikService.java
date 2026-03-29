package ba.maloprodaja.auth.service;

import ba.maloprodaja.auth.dto.KorisnikDTO;

import java.util.List;

public interface IKorisnikService {

    List<KorisnikDTO.KorisnikListItemDTO> listAll(Long idKompanije);

    KorisnikDTO.KorisnikListItemDTO create(KorisnikDTO.CreateKorisnikDTO dto, Long idKompanije);

    KorisnikDTO.KorisnikListItemDTO update(Long id, KorisnikDTO.UpdateKorisnikDTO dto);

    void deactivate(Long id);

    List<String> getAvailableIzbornici();

    List<KorisnikDTO.KorisnikIzbornikaDTO> getIzbornici(Long korisnikId);

    List<KorisnikDTO.KorisnikIzbornikaDTO> updateIzbornici(Long korisnikId, List<KorisnikDTO.KorisnikIzbornikaDTO> izbornici);
}
