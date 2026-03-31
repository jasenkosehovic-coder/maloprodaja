package ba.maloprodaja.sifarnici.grupaartikala.service;

import ba.maloprodaja.common.exception.ResourceNotFoundException;
import ba.maloprodaja.sifarnici.grupaartikala.dto.GrupaArtikalaDTO;
import ba.maloprodaja.sifarnici.grupaartikala.entity.GrupaArtikala;
import ba.maloprodaja.sifarnici.grupaartikala.repository.GrupaArtikalaRepository;
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
public class GrupaArtikalaService implements IGrupaArtikalaService {

    private final GrupaArtikalaRepository grupaArtikalaRepository;

    @Override
    public List<GrupaArtikalaDTO.ListItemDTO> listAll(Long idKompanije) {
        List<GrupaArtikala> grupe = grupaArtikalaRepository.findByIdKompanije(idKompanije);

        Map<Long, String> naziviById = grupe.stream()
                .collect(Collectors.toMap(GrupaArtikala::getId, GrupaArtikala::getNaziv));

        return grupe.stream()
                .map(g -> toListItemDTO(g, naziviById))
                .toList();
    }

    @Override
    @Transactional
    public GrupaArtikalaDTO.ListItemDTO create(GrupaArtikalaDTO.CreateDTO dto, Long idKompanije) {
        if (dto.idRoditeljskeGrupe() != null) {
            grupaArtikalaRepository.findById(dto.idRoditeljskeGrupe())
                    .orElseThrow(() -> new ResourceNotFoundException("GrupaArtikala", dto.idRoditeljskeGrupe()));
        }

        GrupaArtikala grupa = new GrupaArtikala();
        grupa.setNaziv(dto.naziv());
        grupa.setOpis(dto.opis());
        grupa.setAktivan(true);
        grupa.setIdRoditeljskeGrupe(dto.idRoditeljskeGrupe());
        grupa.setIdKompanije(idKompanije);

        GrupaArtikala saved = grupaArtikalaRepository.save(grupa);
        log.info("Kreirana nova grupa artikala: naziv='{}', idKompanije={}", saved.getNaziv(), idKompanije);
        return toListItemDTO(saved, Map.of());
    }

    @Override
    @Transactional
    public GrupaArtikalaDTO.ListItemDTO update(Long id, GrupaArtikalaDTO.UpdateDTO dto) {
        GrupaArtikala grupa = grupaArtikalaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GrupaArtikala", id));

        if (dto.idRoditeljskeGrupe() != null && !dto.idRoditeljskeGrupe().equals(id)) {
            grupaArtikalaRepository.findById(dto.idRoditeljskeGrupe())
                    .orElseThrow(() -> new ResourceNotFoundException("GrupaArtikala", dto.idRoditeljskeGrupe()));
        }

        grupa.setNaziv(dto.naziv());
        grupa.setOpis(dto.opis());
        grupa.setIdRoditeljskeGrupe(dto.idRoditeljskeGrupe());
        if (dto.aktivan() != null) {
            grupa.setAktivan(dto.aktivan());
        }

        GrupaArtikala saved = grupaArtikalaRepository.save(grupa);
        log.info("Ažurirana grupa artikala: id={}", id);
        return toListItemDTO(saved, resolveParentNazivi(saved));
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        GrupaArtikala grupa = grupaArtikalaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GrupaArtikala", id));
        grupa.setAktivan(false);
        grupaArtikalaRepository.save(grupa);
        log.info("Deaktivirana grupa artikala: id={}", id);
    }

    private Map<Long, String> resolveParentNazivi(GrupaArtikala grupa) {
        if (grupa.getIdRoditeljskeGrupe() == null) {
            return Map.of();
        }
        return grupaArtikalaRepository.findById(grupa.getIdRoditeljskeGrupe())
                .map(p -> Map.of(p.getId(), p.getNaziv()))
                .orElse(Map.of());
    }

    private GrupaArtikalaDTO.ListItemDTO toListItemDTO(GrupaArtikala g, Map<Long, String> naziviById) {
        String nazivRoditeljske = g.getIdRoditeljskeGrupe() != null
                ? naziviById.get(g.getIdRoditeljskeGrupe())
                : null;

        return new GrupaArtikalaDTO.ListItemDTO(
                g.getId(),
                g.getNaziv(),
                g.getOpis(),
                g.isAktivan(),
                g.getIdRoditeljskeGrupe(),
                nazivRoditeljske
        );
    }
}
