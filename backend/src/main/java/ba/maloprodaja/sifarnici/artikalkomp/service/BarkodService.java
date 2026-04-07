package ba.maloprodaja.sifarnici.artikalkomp.service;

import ba.maloprodaja.common.exception.BusinessException;
import ba.maloprodaja.common.exception.ResourceNotFoundException;
import ba.maloprodaja.sifarnici.artikalkomp.dto.BarkodDTO;
import ba.maloprodaja.sifarnici.artikalkomp.entity.Barkod;
import ba.maloprodaja.sifarnici.artikalkomp.repository.BarkodRepository;
import ba.maloprodaja.sifarnici.varijanta.entity.VarijantaArtikla;
import ba.maloprodaja.sifarnici.varijanta.repository.VarijantaArtiklaRepository;
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
    private final VarijantaArtiklaRepository varijantaRepository;

    @Override
    public List<BarkodDTO.ListItemDTO> listAll(Long idKompanije) {
        List<Barkod> barkodovi = barkodRepository.findByIdKompanije(idKompanije);

        List<Long> varijantaIds = barkodovi.stream()
                .map(Barkod::getIdVarijante)
                .distinct()
                .toList();

        Map<Long, VarijantaArtikla> varijanteMap = varijantaRepository.findAllById(varijantaIds)
                .stream()
                .collect(Collectors.toMap(VarijantaArtikla::getId, v -> v));

        return barkodovi.stream()
                .map(b -> toDTO(b, varijanteMap.get(b.getIdVarijante())))
                .toList();
    }

    @Override
    public List<BarkodDTO.ListItemDTO> listByVarijanta(Long idVarijante) {
        VarijantaArtikla varijanta = varijantaRepository.findById(idVarijante)
                .orElseThrow(() -> new ResourceNotFoundException("VarijantaArtikla", idVarijante));

        return barkodRepository.findByIdVarijanteAndAktivanTrue(idVarijante)
                .stream()
                .map(b -> toDTO(b, varijanta))
                .toList();
    }

    @Override
    @Transactional
    public BarkodDTO.ListItemDTO create(BarkodDTO.CreateDTO dto, Long idKompanije) {
        VarijantaArtikla varijanta = varijantaRepository.findById(dto.idVarijante())
                .orElseThrow(() -> new ResourceNotFoundException("VarijantaArtikla", dto.idVarijante()));

        if (!varijanta.getIdKompanije().equals(idKompanije)) {
            throw new BusinessException("Varijanta sa ID=" + dto.idVarijante() + " ne pripada ovoj kompaniji.");
        }

        if (barkodRepository.existsByBarkodAndIdKompanije(dto.barkod(), idKompanije)) {
            throw new BusinessException("Barkod '" + dto.barkod() + "' već postoji u ovoj kompaniji.");
        }

        Barkod b = new Barkod();
        b.setBarkod(dto.barkod());
        b.setIdVarijante(dto.idVarijante());
        b.setIdKompanije(idKompanije);
        b.setAktivan(true);

        Barkod saved = barkodRepository.save(b);
        log.info("Kreiran barkod '{}' za varijantu id={}, idKompanije={}", saved.getBarkod(), saved.getIdVarijante(), idKompanije);

        return toDTO(saved, varijanta);
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
        if (dto.aktivan() != null) {
            b.setAktivan(dto.aktivan());
        }

        Barkod saved = barkodRepository.save(b);
        log.info("Ažuriran barkod: id={}", id);

        VarijantaArtikla varijanta = varijantaRepository.findById(saved.getIdVarijante()).orElse(null);
        return toDTO(saved, varijanta);
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

    private BarkodDTO.ListItemDTO toDTO(Barkod b, VarijantaArtikla varijanta) {
        String oznakaVelicine = resolveOznakaVelicine(varijanta);
        return new BarkodDTO.ListItemDTO(
                b.getId(),
                b.getBarkod(),
                b.getIdVarijante(),
                oznakaVelicine,
                b.isAktivan()
        );
    }

    private String resolveOznakaVelicine(VarijantaArtikla varijanta) {
        if (varijanta == null) {
            return null;
        }
        if (varijanta.getVelicina() != null) {
            return varijanta.getVelicina().getOznaka();
        }
        if (varijanta.getIdVelicine() == null) {
            return "Default";
        }
        return null;
    }
}
