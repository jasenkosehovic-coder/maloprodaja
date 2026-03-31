package ba.maloprodaja.sifarnici.proizvodjac.service;

import ba.maloprodaja.common.exception.ResourceNotFoundException;
import ba.maloprodaja.sifarnici.proizvodjac.dto.ProizvodjacDTO;
import ba.maloprodaja.sifarnici.proizvodjac.entity.Proizvodjac;
import ba.maloprodaja.sifarnici.proizvodjac.repository.ProizvodjacRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProizvodjacService implements IProizvodjacService {

    private final ProizvodjacRepository proizvodjacRepository;

    @Override
    public List<ProizvodjacDTO.ListItemDTO> listAll(Long idKompanije) {
        return proizvodjacRepository.findByIdKompanije(idKompanije)
                .stream()
                .map(this::toListItemDTO)
                .toList();
    }

    @Override
    @Transactional
    public ProizvodjacDTO.ListItemDTO create(ProizvodjacDTO.CreateDTO dto, Long idKompanije) {
        Proizvodjac p = new Proizvodjac();
        p.setNaziv(dto.naziv());
        p.setDrzava(dto.drzava());
        p.setKontaktOsoba(dto.kontaktOsoba());
        p.setTelefon(dto.telefon());
        p.setEmail(dto.email());
        p.setAktivan(true);
        p.setIdKompanije(idKompanije);

        Proizvodjac saved = proizvodjacRepository.save(p);
        log.info("Kreiran novi proizvođač: naziv='{}', idKompanije={}", saved.getNaziv(), idKompanije);
        return toListItemDTO(saved);
    }

    @Override
    @Transactional
    public ProizvodjacDTO.ListItemDTO update(Long id, ProizvodjacDTO.UpdateDTO dto) {
        Proizvodjac p = proizvodjacRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proizvodjac", id));

        p.setNaziv(dto.naziv());
        p.setDrzava(dto.drzava());
        p.setKontaktOsoba(dto.kontaktOsoba());
        p.setTelefon(dto.telefon());
        p.setEmail(dto.email());
        if (dto.aktivan() != null) {
            p.setAktivan(dto.aktivan());
        }

        Proizvodjac saved = proizvodjacRepository.save(p);
        log.info("Ažuriran proizvođač: id={}", id);
        return toListItemDTO(saved);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Proizvodjac p = proizvodjacRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proizvodjac", id));
        p.setAktivan(false);
        proizvodjacRepository.save(p);
        log.info("Deaktiviran proizvođač: id={}", id);
    }

    private ProizvodjacDTO.ListItemDTO toListItemDTO(Proizvodjac p) {
        return new ProizvodjacDTO.ListItemDTO(
                p.getId(),
                p.getNaziv(),
                p.getDrzava(),
                p.getKontaktOsoba(),
                p.getTelefon(),
                p.getEmail(),
                p.isAktivan()
        );
    }
}
