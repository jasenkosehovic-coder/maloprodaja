package ba.maloprodaja.sifarnici.artikalkomp.service;

import ba.maloprodaja.common.exception.BusinessException;
import ba.maloprodaja.common.exception.ResourceNotFoundException;
import ba.maloprodaja.sifarnici.artikalkomp.dto.ArtikalKompDTO;
import ba.maloprodaja.sifarnici.artikalkomp.entity.ArtikalKompanija;
import ba.maloprodaja.sifarnici.artikalkomp.repository.ArtikalKompanijeRepository;
import ba.maloprodaja.sifarnici.dobavljac.entity.Dobavljac;
import ba.maloprodaja.sifarnici.dobavljac.repository.DobavljacRepository;
import ba.maloprodaja.sifarnici.grupaartikala.entity.GrupaArtikala;
import ba.maloprodaja.sifarnici.grupaartikala.repository.GrupaArtikalaRepository;
import ba.maloprodaja.sifarnici.proizvodjac.entity.Proizvodjac;
import ba.maloprodaja.sifarnici.proizvodjac.repository.ProizvodjacRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtikalKompService implements IArtikalKompService {

    private final ArtikalKompanijeRepository artikalKompanijeRepository;
    private final GrupaArtikalaRepository grupaArtikalaRepository;
    private final ProizvodjacRepository proizvodjacRepository;
    private final DobavljacRepository dobavljacRepository;

    @Override
    public List<ArtikalKompDTO.ListItemDTO> listAll(Long idKompanije) {
        List<ArtikalKompanija> artikli = artikalKompanijeRepository.findByIdKompanije(idKompanije);

        List<Long> grupaIds = artikli.stream()
                .map(ArtikalKompanija::getIdGrupe).filter(id -> id != null).distinct().toList();
        List<Long> proizvodjacIds = artikli.stream()
                .map(ArtikalKompanija::getIdProizvodjaca).filter(id -> id != null).distinct().toList();
        List<Long> dobavljacIds = artikli.stream()
                .map(ArtikalKompanija::getIdDobavljaca).filter(id -> id != null).distinct().toList();

        Map<Long, String> grupeNazivi = grupaArtikalaRepository.findAllById(grupaIds).stream()
                .collect(Collectors.toMap(GrupaArtikala::getId, GrupaArtikala::getNaziv));
        Map<Long, String> proizvodjaciNazivi = proizvodjacRepository.findAllById(proizvodjacIds).stream()
                .collect(Collectors.toMap(Proizvodjac::getId, Proizvodjac::getNaziv));
        Map<Long, String> dobavljaciNazivi = dobavljacRepository.findAllById(dobavljacIds).stream()
                .collect(Collectors.toMap(Dobavljac::getId, Dobavljac::getNaziv));

        return artikli.stream()
                .map(a -> toListItemDTO(a, grupeNazivi, proizvodjaciNazivi, dobavljaciNazivi))
                .toList();
    }

    @Override
    @Transactional
    public ArtikalKompDTO.ListItemDTO create(ArtikalKompDTO.CreateDTO dto, Long idKompanije) {
        if (artikalKompanijeRepository.existsBySifraAndIdKompanije(dto.sifra(), idKompanije)) {
            throw new BusinessException("Artikal sa šifrom '" + dto.sifra() + "' već postoji u ovoj kompaniji.");
        }

        validateForeignKeys(dto.idGrupe(), dto.idProizvodjaca(), dto.idDobavljaca());

        ArtikalKompanija a = new ArtikalKompanija();
        applyFields(dto.naziv(), dto.sifra(), dto.opis(), dto.jedin(), dto.pdv(),
                dto.idGrupe(), dto.idProizvodjaca(), dto.idDobavljaca(), a);
        a.setAktivan(true);
        a.setIdKompanije(idKompanije);

        ArtikalKompanija saved = artikalKompanijeRepository.save(a);
        log.info("Kreiran novi artikal: sifra='{}', naziv='{}', idKompanije={}",
                saved.getSifra(), saved.getNaziv(), idKompanije);
        return toListItemDTOSingle(saved);
    }

    @Override
    @Transactional
    public ArtikalKompDTO.ListItemDTO update(Long id, ArtikalKompDTO.UpdateDTO dto) {
        ArtikalKompanija a = artikalKompanijeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ArtikalKompanija", id));

        if (!a.getSifra().equals(dto.sifra()) &&
                artikalKompanijeRepository.existsBySifraAndIdKompanije(dto.sifra(), a.getIdKompanije())) {
            throw new BusinessException("Artikal sa šifrom '" + dto.sifra() + "' već postoji u ovoj kompaniji.");
        }

        validateForeignKeys(dto.idGrupe(), dto.idProizvodjaca(), dto.idDobavljaca());

        applyFields(dto.naziv(), dto.sifra(), dto.opis(), dto.jedin(), dto.pdv(),
                dto.idGrupe(), dto.idProizvodjaca(), dto.idDobavljaca(), a);
        if (dto.aktivan() != null) {
            a.setAktivan(dto.aktivan());
        }

        ArtikalKompanija saved = artikalKompanijeRepository.save(a);
        log.info("Ažuriran artikal: id={}", id);
        return toListItemDTOSingle(saved);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        ArtikalKompanija a = artikalKompanijeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ArtikalKompanija", id));
        a.setAktivan(false);
        artikalKompanijeRepository.save(a);
        log.info("Deaktiviran artikal: id={}", id);
    }

    // ---- Private helpers ----

    private void applyFields(String naziv, String sifra, String opis, String jedin,
                              BigDecimal pdv,
                              Long idGrupe, Long idProizvodjaca, Long idDobavljaca,
                              ArtikalKompanija a) {
        a.setNaziv(naziv);
        a.setSifra(sifra);
        a.setOpis(opis);
        a.setJedin(jedin);
        a.setPdv(pdv);
        a.setIdGrupe(idGrupe);
        a.setIdProizvodjaca(idProizvodjaca);
        a.setIdDobavljaca(idDobavljaca);
    }

    private void validateForeignKeys(Long idGrupe, Long idProizvodjaca, Long idDobavljaca) {
        if (idGrupe != null) {
            grupaArtikalaRepository.findById(idGrupe)
                    .orElseThrow(() -> new ResourceNotFoundException("GrupaArtikala", idGrupe));
        }
        if (idProizvodjaca != null) {
            proizvodjacRepository.findById(idProizvodjaca)
                    .orElseThrow(() -> new ResourceNotFoundException("Proizvodjac", idProizvodjaca));
        }
        if (idDobavljaca != null) {
            dobavljacRepository.findById(idDobavljaca)
                    .orElseThrow(() -> new ResourceNotFoundException("Dobavljac", idDobavljaca));
        }
    }

    private ArtikalKompDTO.ListItemDTO toListItemDTOSingle(ArtikalKompanija a) {
        String nazivGrupe = a.getIdGrupe() != null
                ? grupaArtikalaRepository.findById(a.getIdGrupe()).map(GrupaArtikala::getNaziv).orElse(null)
                : null;
        String nazivProizvodjaca = a.getIdProizvodjaca() != null
                ? proizvodjacRepository.findById(a.getIdProizvodjaca()).map(Proizvodjac::getNaziv).orElse(null)
                : null;
        String nazivDobavljaca = a.getIdDobavljaca() != null
                ? dobavljacRepository.findById(a.getIdDobavljaca()).map(Dobavljac::getNaziv).orElse(null)
                : null;

        return buildDTO(a, nazivGrupe, nazivProizvodjaca, nazivDobavljaca);
    }

    private ArtikalKompDTO.ListItemDTO toListItemDTO(ArtikalKompanija a,
                                                      Map<Long, String> grupeNazivi,
                                                      Map<Long, String> proizvodjaciNazivi,
                                                      Map<Long, String> dobavljaciNazivi) {
        return buildDTO(
                a,
                a.getIdGrupe() != null ? grupeNazivi.get(a.getIdGrupe()) : null,
                a.getIdProizvodjaca() != null ? proizvodjaciNazivi.get(a.getIdProizvodjaca()) : null,
                a.getIdDobavljaca() != null ? dobavljaciNazivi.get(a.getIdDobavljaca()) : null
        );
    }

    private ArtikalKompDTO.ListItemDTO buildDTO(ArtikalKompanija a,
                                                 String nazivGrupe,
                                                 String nazivProizvodjaca,
                                                 String nazivDobavljaca) {
        return new ArtikalKompDTO.ListItemDTO(
                a.getId(),
                a.getNaziv(),
                a.getSifra(),
                a.getOpis(),
                a.getJedin(),
                a.getPdv(),
                a.isAktivan(),
                a.getIdGrupe(),
                nazivGrupe,
                a.getIdProizvodjaca(),
                nazivProizvodjaca,
                a.getIdDobavljaca(),
                nazivDobavljaca
        );
    }
}
