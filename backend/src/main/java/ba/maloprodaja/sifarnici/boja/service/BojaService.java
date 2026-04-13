package ba.maloprodaja.sifarnici.boja.service;

import ba.maloprodaja.common.exception.BusinessException;
import ba.maloprodaja.common.exception.ResourceNotFoundException;
import ba.maloprodaja.sifarnici.boja.dto.BojaDTO;
import ba.maloprodaja.sifarnici.boja.entity.Boja;
import ba.maloprodaja.sifarnici.boja.repository.BojaRepository;
import ba.maloprodaja.sifarnici.varijanta.repository.VarijantaArtiklaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BojaService implements IBojaService {

    private final BojaRepository bojaRepository;
    private final VarijantaArtiklaRepository varijantaArtiklaRepository;

    @Override
    public List<BojaDTO.ListItemDTO> listAll(Long idKompanije) {
        return bojaRepository.findByIdKompanijeOrderByNazivAsc(idKompanije)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public List<BojaDTO.ListItemDTO> listAktivne(Long idKompanije) {
        return bojaRepository.findByIdKompanijeAndAktivanTrueOrderByNazivAsc(idKompanije)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public BojaDTO.ListItemDTO create(BojaDTO.CreateDTO dto, Long idKompanije) {
        if (bojaRepository.existsByNazivAndIdKompanije(dto.naziv(), idKompanije)) {
            throw new BusinessException("Boja sa nazivom '" + dto.naziv() + "' već postoji u ovoj kompaniji.");
        }

        Boja boja = new Boja();
        boja.setNaziv(dto.naziv());
        boja.setHexKod(dto.hexKod());
        boja.setAktivan(true);
        boja.setIdKompanije(idKompanije);

        Boja saved = bojaRepository.save(boja);
        log.info("Kreirana nova boja: naziv='{}', idKompanije={}", saved.getNaziv(), idKompanije);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public BojaDTO.ListItemDTO update(Long id, BojaDTO.UpdateDTO dto, Long idKompanije) {
        Boja boja = findByIdAndKompanija(id, idKompanije);

        if (!boja.getNaziv().equals(dto.naziv()) &&
                bojaRepository.existsByNazivAndIdKompanijeAndIdNot(dto.naziv(), idKompanije, id)) {
            throw new BusinessException("Boja sa nazivom '" + dto.naziv() + "' već postoji u ovoj kompaniji.");
        }

        boja.setNaziv(dto.naziv());
        boja.setHexKod(dto.hexKod());
        if (dto.aktivan() != null) {
            boja.setAktivan(dto.aktivan());
        }

        Boja saved = bojaRepository.save(boja);
        log.info("Ažurirana boja: id={}", id);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public void delete(Long id, Long idKompanije) {
        Boja boja = findByIdAndKompanija(id, idKompanije);

        if (varijantaArtiklaRepository.existsByIdBojeAndAktivanTrue(id)) {
            throw new BusinessException(
                    "Nije moguće deaktivirati boju koja se koristi u aktivnim varijantama artikala.");
        }

        boja.setAktivan(false);
        bojaRepository.save(boja);
        log.info("Deaktivirana boja: id={}", id);
    }

    // ---- Private helpers ----

    private Boja findByIdAndKompanija(Long id, Long idKompanije) {
        Boja boja = bojaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Boja", id));
        if (!boja.getIdKompanije().equals(idKompanije)) {
            throw new ResourceNotFoundException("Boja", id);
        }
        return boja;
    }

    private BojaDTO.ListItemDTO toDTO(Boja boja) {
        return new BojaDTO.ListItemDTO(
                boja.getId(),
                boja.getNaziv(),
                boja.getHexKod(),
                boja.isAktivan()
        );
    }
}
