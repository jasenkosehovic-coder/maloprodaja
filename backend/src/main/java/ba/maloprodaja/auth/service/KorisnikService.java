package ba.maloprodaja.auth.service;

import ba.maloprodaja.auth.dto.KorisnikDTO;
import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.auth.entity.KorisnikIzbornik;
import ba.maloprodaja.auth.repository.KorisnikIzborniciRepository;
import ba.maloprodaja.auth.repository.KorisnikRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KorisnikService implements IKorisnikService {

    private final KorisnikRepository korisnikRepository;
    private final KorisnikIzborniciRepository korisnikIzborniciRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<KorisnikDTO.KorisnikListItemDTO> listAll(Long idKompanije) {
        return korisnikRepository.findByIdKompanije(idKompanije)
                .stream()
                .map(this::toListItemDTO)
                .toList();
    }

    @Override
    @Transactional
    public KorisnikDTO.KorisnikListItemDTO create(KorisnikDTO.CreateKorisnikDTO dto, Long idKompanije) {
        Korisnik korisnik = new Korisnik();
        korisnik.setUsername(dto.username());
        korisnik.setPasswordHash(passwordEncoder.encode(dto.password()));
        korisnik.setIme(dto.ime());
        korisnik.setPrezime(dto.prezime());
        korisnik.setEmail(dto.email());
        korisnik.setUloga(dto.uloga());
        korisnik.setAktivan(true);
        korisnik.setIdKompanije(idKompanije);
        korisnik.setIdPoslovnice(dto.poslovnicaId());

        Korisnik saved = korisnikRepository.save(korisnik);
        log.info("Kreiran novi korisnik: username='{}'", saved.getUsername());
        return toListItemDTO(saved);
    }

    @Override
    @Transactional
    public KorisnikDTO.KorisnikListItemDTO update(Long id, KorisnikDTO.UpdateKorisnikDTO dto) {
        Korisnik korisnik = korisnikRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Korisnik nije pronađen: " + id));

        if (dto.ime() != null) korisnik.setIme(dto.ime());
        if (dto.prezime() != null) korisnik.setPrezime(dto.prezime());
        if (dto.email() != null) korisnik.setEmail(dto.email());
        if (dto.uloga() != null) korisnik.setUloga(dto.uloga());
        if (dto.poslovnicaId() != null) korisnik.setIdPoslovnice(dto.poslovnicaId());
        if (dto.aktivan() != null) korisnik.setAktivan(dto.aktivan());

        return toListItemDTO(korisnikRepository.save(korisnik));
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Korisnik korisnik = korisnikRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Korisnik nije pronađen: " + id));
        korisnik.setAktivan(false);
        korisnikRepository.save(korisnik);
        log.info("Korisnik deaktiviran: id={}", id);
    }

    @Override
    public List<KorisnikDTO.KorisnikIzbornikaDTO> getIzbornici(Long korisnikId) {
        return korisnikIzborniciRepository.findByKorisnikId(korisnikId)
                .stream()
                .map(i -> new KorisnikDTO.KorisnikIzbornikaDTO(i.getIzbornikKljuc(), i.isAktivan()))
                .toList();
    }

    @Override
    @Transactional
    public List<KorisnikDTO.KorisnikIzbornikaDTO> updateIzbornici(Long korisnikId, List<KorisnikDTO.KorisnikIzbornikaDTO> izbornici) {
        Korisnik k = korisnikRepository.findById(korisnikId)
                .orElseThrow(() -> new EntityNotFoundException("Korisnik nije pronađen: " + korisnikId));

        korisnikIzborniciRepository.deleteByKorisnikId(korisnikId);

        List<KorisnikIzbornik> entities = izbornici.stream().map(dto -> {
            KorisnikIzbornik e = new KorisnikIzbornik();
            e.setKorisnikId(korisnikId);
            e.setIzbornikKljuc(dto.izbornikKljuc());
            e.setAktivan(dto.aktivan());
            e.setIdKompanije(k.getIdKompanije());
            e.setIdPoslovnice(k.getIdPoslovnice());
            return e;
        }).toList();

        korisnikIzborniciRepository.saveAll(entities);
        return getIzbornici(korisnikId);
    }

    private KorisnikDTO.KorisnikListItemDTO toListItemDTO(Korisnik k) {
        return new KorisnikDTO.KorisnikListItemDTO(
                k.getId(),
                k.getUsername(),
                k.getIme(),
                k.getPrezime(),
                k.getEmail(),
                k.getUloga(),
                k.getIdPoslovnice(),
                k.isAktivan()
        );
    }
}
