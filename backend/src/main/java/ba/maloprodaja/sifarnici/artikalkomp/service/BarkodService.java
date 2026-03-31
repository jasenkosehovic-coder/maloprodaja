package ba.maloprodaja.sifarnici.artikalkomp.service;

import ba.maloprodaja.common.exception.BusinessException;
import ba.maloprodaja.common.exception.ResourceNotFoundException;
import ba.maloprodaja.poslovnica.repository.PoslovnicaRepository;
import ba.maloprodaja.sifarnici.artikalkomp.dto.BarkodDTO;
import ba.maloprodaja.sifarnici.artikalkomp.entity.ArtikalKompanija;
import ba.maloprodaja.sifarnici.artikalkomp.entity.Barkod;
import ba.maloprodaja.sifarnici.artikalkomp.repository.ArtikalKompanijeRepository;
import ba.maloprodaja.sifarnici.artikalkomp.repository.BarkodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BarkodService implements IBarkodService {

    private final BarkodRepository barkodRepository;
    private final ArtikalKompanijeRepository artikalKompanijeRepository;
    private final PoslovnicaRepository poslovnicaRepository;

    @Override
    public List<BarkodDTO.ListItemDTO> listByKompanija(Long idKompanije) {
        List<Barkod> barkodovi = barkodRepository.findByIdKompanije(idKompanije);

        List<Long> artikalIds = barkodovi.stream()
                .map(Barkod::getIdArtikla)
                .distinct()
                .toList();

        Map<Long, ArtikalKompanija> artikliMap = artikalKompanijeRepository.findAllById(artikalIds)
                .stream()
                .collect(Collectors.toMap(ArtikalKompanija::getId, a -> a));

        List<Long> poslovnicaIds = barkodovi.stream()
                .map(Barkod::getIdPoslovnice)
                .filter(id -> id != null)
                .distinct()
                .toList();

        Map<Long, String> poslovniceNazivi = poslovnicaRepository.findAllById(poslovnicaIds)
                .stream()
                .collect(Collectors.toMap(p -> p.getId(), p -> p.getNaziv()));

        return barkodovi.stream()
                .map(b -> toDTO(b, artikliMap.get(b.getIdArtikla()), poslovniceNazivi))
                .toList();
    }

    @Override
    @Transactional
    public BarkodDTO.ListItemDTO create(BarkodDTO.CreateDTO dto, Long idKompanije, Long idPoslovnice) {
        ArtikalKompanija artikal = artikalKompanijeRepository.findById(dto.idArtikla())
                .orElseThrow(() -> new ResourceNotFoundException("ArtikalKompanija", dto.idArtikla()));

        if (barkodRepository.existsByBarkodAndIdKompanije(dto.barkod(), idKompanije)) {
            throw new BusinessException("Barkod '" + dto.barkod() + "' već postoji u ovoj kompaniji.");
        }

        Barkod b = new Barkod();
        b.setBarkod(dto.barkod());
        b.setIdArtikla(dto.idArtikla());
        b.setIdKompanije(idKompanije);
        b.setIdPoslovnice(idPoslovnice);
        b.setAktivan(true);

        Barkod saved = barkodRepository.save(b);
        log.info("Kreiran barkod '{}' za artikal id={}, idKompanije={}, idPoslovnice={}",
                saved.getBarkod(), saved.getIdArtikla(), idKompanije, idPoslovnice);

        String poslovnicaNaziv = resolvePoslovnicaNaziv(idPoslovnice);
        return toDTO(saved, artikal, poslovnicaNaziv);
    }

    @Override
    @Transactional
    public BarkodDTO.ListItemDTO update(Long id, BarkodDTO.UpdateDTO dto) {
        Barkod b = barkodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barkod", id));

        if (!b.getBarkod().equals(dto.barkod()) &&
                barkodRepository.existsByBarkodAndIdKompanijeAndIdNot(dto.barkod(), b.getIdKompanije(), id)) {
            throw new BusinessException("Barkod '" + dto.barkod() + "' već postoji u ovoj kompaniji.");
        }

        b.setBarkod(dto.barkod());
        b.setIdPoslovnice(dto.idPoslovnice());
        if (dto.aktivan() != null) {
            b.setAktivan(dto.aktivan());
        }

        Barkod saved = barkodRepository.save(b);
        log.info("Ažuriran barkod: id={}", id);

        ArtikalKompanija artikal = artikalKompanijeRepository.findById(saved.getIdArtikla()).orElse(null);
        String poslovnicaNaziv = resolvePoslovnicaNaziv(saved.getIdPoslovnice());
        return toDTO(saved, artikal, poslovnicaNaziv);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Barkod b = barkodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barkod", id));
        b.setAktivan(false);
        barkodRepository.save(b);
        log.info("Deaktiviran barkod: id={}", id);
    }

    // ---- Private helpers ----

    private String resolvePoslovnicaNaziv(Long idPoslovnice) {
        if (idPoslovnice == null) {
            return null;
        }
        return poslovnicaRepository.findById(idPoslovnice)
                .map(p -> p.getNaziv())
                .orElse(null);
    }

    private BarkodDTO.ListItemDTO toDTO(Barkod b, ArtikalKompanija artikal,
                                        Map<Long, String> poslovniceNazivi) {
        String artikalNaziv = artikal != null ? artikal.getNaziv() : null;
        String artikalSifra = artikal != null ? artikal.getSifra() : null;
        String poslovnicaNaziv = b.getIdPoslovnice() != null ? poslovniceNazivi.get(b.getIdPoslovnice()) : null;
        return new BarkodDTO.ListItemDTO(
                b.getId(), b.getBarkod(), b.getIdArtikla(),
                artikalNaziv, artikalSifra,
                b.getIdPoslovnice(), poslovnicaNaziv,
                b.isAktivan()
        );
    }

    private BarkodDTO.ListItemDTO toDTO(Barkod b, ArtikalKompanija artikal, String poslovnicaNaziv) {
        String artikalNaziv = artikal != null ? artikal.getNaziv() : null;
        String artikalSifra = artikal != null ? artikal.getSifra() : null;
        return new BarkodDTO.ListItemDTO(
                b.getId(), b.getBarkod(), b.getIdArtikla(),
                artikalNaziv, artikalSifra,
                b.getIdPoslovnice(), poslovnicaNaziv,
                b.isAktivan()
        );
    }
}
