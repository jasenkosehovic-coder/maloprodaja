package ba.maloprodaja.sifarnici.velicina.service;

import ba.maloprodaja.common.exception.BusinessException;
import ba.maloprodaja.common.exception.ResourceNotFoundException;
import ba.maloprodaja.sifarnici.velicina.dto.TipVelicinaDTO;
import ba.maloprodaja.sifarnici.velicina.dto.VelicinaDTO;
import ba.maloprodaja.sifarnici.velicina.entity.TipVelicina;
import ba.maloprodaja.sifarnici.velicina.entity.Velicina;
import ba.maloprodaja.sifarnici.velicina.repository.TipVelicinaRepository;
import ba.maloprodaja.sifarnici.velicina.repository.VelicinaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TipVelicinaService implements ITipVelicinaService {

    private final TipVelicinaRepository tipVelicinaRepository;
    private final VelicinaRepository velicinaRepository;

    @Override
    public List<TipVelicinaDTO.ListItemDTO> listAll(Long idKompanije) {
        return tipVelicinaRepository.findByIdKompanijeOrderByNazivAsc(idKompanije)
                .stream()
                .map(this::toTipDTO)
                .toList();
    }

    @Override
    @Transactional
    public TipVelicinaDTO.ListItemDTO create(TipVelicinaDTO.CreateDTO dto, Long idKompanije) {
        if (tipVelicinaRepository.existsByNazivAndIdKompanije(dto.naziv(), idKompanije)) {
            throw new BusinessException("Tip veličine sa nazivom '" + dto.naziv() + "' već postoji u ovoj kompaniji.");
        }

        TipVelicina tip = new TipVelicina();
        tip.setNaziv(dto.naziv());
        tip.setOpis(dto.opis());
        tip.setAktivan(true);
        tip.setIdKompanije(idKompanije);

        TipVelicina saved = tipVelicinaRepository.save(tip);
        log.info("Kreiran novi tip veličine: naziv='{}', idKompanije={}", saved.getNaziv(), idKompanije);
        return toTipDTO(saved);
    }

    @Override
    @Transactional
    public TipVelicinaDTO.ListItemDTO update(Long id, TipVelicinaDTO.UpdateDTO dto) {
        TipVelicina tip = tipVelicinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipVelicina", id));

        if (!tip.getNaziv().equals(dto.naziv()) &&
                tipVelicinaRepository.existsByNazivAndIdKompanijeAndIdNot(dto.naziv(), tip.getIdKompanije(), id)) {
            throw new BusinessException("Tip veličine sa nazivom '" + dto.naziv() + "' već postoji u ovoj kompaniji.");
        }

        tip.setNaziv(dto.naziv());
        tip.setOpis(dto.opis());
        if (dto.aktivan() != null) {
            tip.setAktivan(dto.aktivan());
        }

        TipVelicina saved = tipVelicinaRepository.save(tip);
        log.info("Ažuriran tip veličine: id={}", id);
        return toTipDTO(saved);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        TipVelicina tip = tipVelicinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TipVelicina", id));

        if (velicinaRepository.existsByIdTipaVelicinaAndAktivanTrue(id)) {
            throw new BusinessException("Nije moguće obrisati tip veličine koji ima aktivnih veličina.");
        }

        tip.setAktivan(false);
        tipVelicinaRepository.save(tip);
        log.info("Deaktiviran tip veličine: id={}", id);
    }

    @Override
    public List<VelicinaDTO.ListItemDTO> listVelicine(Long idTipa) {
        tipVelicinaRepository.findById(idTipa)
                .orElseThrow(() -> new ResourceNotFoundException("TipVelicina", idTipa));

        return velicinaRepository.findByIdTipaVelicinaOrderByRedosljed(idTipa)
                .stream()
                .map(this::toVelicinaDTO)
                .toList();
    }

    @Override
    @Transactional
    public VelicinaDTO.ListItemDTO addVelicina(Long idTipa, VelicinaDTO.CreateDTO dto, Long idKompanije) {
        TipVelicina tip = tipVelicinaRepository.findById(idTipa)
                .orElseThrow(() -> new ResourceNotFoundException("TipVelicina", idTipa));

        if (!tip.getIdKompanije().equals(idKompanije)) {
            throw new ResourceNotFoundException("TipVelicina", idTipa);
        }

        Velicina velicina = new Velicina();
        velicina.setIdTipaVelicina(idTipa);
        velicina.setOznaka(dto.oznaka());
        velicina.setRedosljed(dto.redosljed());
        velicina.setAktivan(true);
        velicina.setIdKompanije(idKompanije);

        Velicina saved = velicinaRepository.save(velicina);
        log.info("Dodana veličina: oznaka='{}', idTipa={}, idKompanije={}", saved.getOznaka(), idTipa, idKompanije);
        return toVelicinaDTO(saved);
    }

    @Override
    @Transactional
    public VelicinaDTO.ListItemDTO updateVelicina(Long id, VelicinaDTO.UpdateDTO dto) {
        Velicina velicina = velicinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Velicina", id));

        if (!velicina.getOznaka().equals(dto.oznaka()) &&
                velicinaRepository.existsByIdTipaVelicinaAndOznakaAndIdNot(
                        velicina.getIdTipaVelicina(), dto.oznaka(), id)) {
            throw new BusinessException("Veličina sa oznakom '" + dto.oznaka() + "' već postoji u ovom tipu.");
        }

        velicina.setOznaka(dto.oznaka());
        velicina.setRedosljed(dto.redosljed());
        if (dto.aktivan() != null) {
            velicina.setAktivan(dto.aktivan());
        }

        Velicina saved = velicinaRepository.save(velicina);
        log.info("Ažurirana veličina: id={}", id);
        return toVelicinaDTO(saved);
    }

    @Override
    @Transactional
    public void deactivateVelicina(Long id) {
        Velicina velicina = velicinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Velicina", id));
        velicina.setAktivan(false);
        velicinaRepository.save(velicina);
        log.info("Deaktivirana veličina: id={}", id);
    }

    // ---- Private helpers ----

    private TipVelicinaDTO.ListItemDTO toTipDTO(TipVelicina tip) {
        return new TipVelicinaDTO.ListItemDTO(
                tip.getId(),
                tip.getNaziv(),
                tip.getOpis(),
                tip.isAktivan()
        );
    }

    private VelicinaDTO.ListItemDTO toVelicinaDTO(Velicina v) {
        return new VelicinaDTO.ListItemDTO(
                v.getId(),
                v.getIdTipaVelicina(),
                v.getOznaka(),
                v.getRedosljed(),
                v.isAktivan()
        );
    }
}
