package ba.maloprodaja.sifarnici.popust.service;

import ba.maloprodaja.common.exception.ResourceNotFoundException;
import ba.maloprodaja.poslovnica.entity.Poslovnica;
import ba.maloprodaja.poslovnica.repository.PoslovnicaRepository;
import ba.maloprodaja.sifarnici.popust.dto.PopustDTO;
import ba.maloprodaja.sifarnici.popust.entity.Popust;
import ba.maloprodaja.sifarnici.popust.repository.PopustRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopustService implements IPopustService {

    private final PopustRepository popustRepository;
    private final PoslovnicaRepository poslovnicaRepository;

    @Override
    public List<PopustDTO.ListItemDTO> listAll(Long idKompanije) {
        return popustRepository.findByIdKompanije(idKompanije)
                .stream()
                .map(this::toListItemDTO)
                .toList();
    }

    @Override
    @Transactional
    public PopustDTO.ListItemDTO create(PopustDTO.CreateDTO dto, Long idKompanije) {
        Popust p = new Popust();
        p.setNaziv(dto.naziv());
        p.setProcenat(dto.procenat());
        p.setDatumOd(dto.datumOd());
        p.setDatumDo(dto.datumDo());
        p.setAktivan(true);
        p.setIdKompanije(idKompanije);
        p.setIdPoslovnice(dto.idPoslovnice());

        Popust saved = popustRepository.save(p);
        log.info("Kreiran novi popust: naziv='{}', procenat={}, idKompanije={}", saved.getNaziv(), saved.getProcenat(), idKompanije);
        return toListItemDTO(saved);
    }

    @Override
    @Transactional
    public PopustDTO.ListItemDTO update(Long id, PopustDTO.UpdateDTO dto) {
        Popust p = popustRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Popust", id));

        p.setNaziv(dto.naziv());
        p.setProcenat(dto.procenat());
        p.setDatumOd(dto.datumOd());
        p.setDatumDo(dto.datumDo());
        if (dto.aktivan() != null) {
            p.setAktivan(dto.aktivan());
        }
        p.setIdPoslovnice(dto.idPoslovnice());

        Popust saved = popustRepository.save(p);
        log.info("Ažuriran popust: id={}", id);
        return toListItemDTO(saved);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Popust p = popustRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Popust", id));
        p.setAktivan(false);
        popustRepository.save(p);
        log.info("Deaktiviran popust: id={}", id);
    }

    private PopustDTO.ListItemDTO toListItemDTO(Popust p) {
        String poslovnicaNaziv = p.getIdPoslovnice() != null
                ? poslovnicaRepository.findById(p.getIdPoslovnice())
                        .map(Poslovnica::getNaziv).orElse(null)
                : null;

        return new PopustDTO.ListItemDTO(
                p.getId(),
                p.getNaziv(),
                p.getProcenat(),
                p.getDatumOd(),
                p.getDatumDo(),
                p.isAktivan(),
                p.getIdPoslovnice(),
                poslovnicaNaziv
        );
    }
}
