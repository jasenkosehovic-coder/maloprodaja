package ba.maloprodaja.sifarnici.dobavljac.service;

import ba.maloprodaja.common.exception.ResourceNotFoundException;
import ba.maloprodaja.sifarnici.dobavljac.dto.DobavljacDTO;
import ba.maloprodaja.sifarnici.dobavljac.entity.Dobavljac;
import ba.maloprodaja.sifarnici.dobavljac.repository.DobavljacRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DobavljacService implements IDobavljacService {

    private final DobavljacRepository dobavljacRepository;

    @Override
    public List<DobavljacDTO.ListItemDTO> listAll(Long idKompanije) {
        return dobavljacRepository.findByIdKompanije(idKompanije)
                .stream()
                .map(this::toListItemDTO)
                .toList();
    }

    @Override
    @Transactional
    public DobavljacDTO.ListItemDTO create(DobavljacDTO.CreateDTO dto, Long idKompanije) {
        Dobavljac d = new Dobavljac();
        d.setNaziv(dto.naziv());
        d.setAdresa(dto.adresa());
        d.setGrad(dto.grad());
        d.setTelefon(dto.telefon());
        d.setEmail(dto.email());
        d.setPib(dto.pib());
        d.setAktivan(true);
        d.setIdKompanije(idKompanije);

        Dobavljac saved = dobavljacRepository.save(d);
        log.info("Kreiran novi dobavljač: naziv='{}', idKompanije={}", saved.getNaziv(), idKompanije);
        return toListItemDTO(saved);
    }

    @Override
    @Transactional
    public DobavljacDTO.ListItemDTO update(Long id, DobavljacDTO.UpdateDTO dto) {
        Dobavljac d = dobavljacRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dobavljac", id));

        d.setNaziv(dto.naziv());
        d.setAdresa(dto.adresa());
        d.setGrad(dto.grad());
        d.setTelefon(dto.telefon());
        d.setEmail(dto.email());
        d.setPib(dto.pib());
        if (dto.aktivan() != null) {
            d.setAktivan(dto.aktivan());
        }

        Dobavljac saved = dobavljacRepository.save(d);
        log.info("Ažuriran dobavljač: id={}", id);
        return toListItemDTO(saved);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Dobavljac d = dobavljacRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dobavljac", id));
        d.setAktivan(false);
        dobavljacRepository.save(d);
        log.info("Deaktiviran dobavljač: id={}", id);
    }

    private DobavljacDTO.ListItemDTO toListItemDTO(Dobavljac d) {
        return new DobavljacDTO.ListItemDTO(
                d.getId(),
                d.getNaziv(),
                d.getAdresa(),
                d.getGrad(),
                d.getTelefon(),
                d.getEmail(),
                d.getPib(),
                d.isAktivan()
        );
    }
}
