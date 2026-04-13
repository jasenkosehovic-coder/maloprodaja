package ba.maloprodaja.sifarnici.artikalkomp.service;

import ba.maloprodaja.common.exception.BusinessException;
import ba.maloprodaja.common.exception.ResourceNotFoundException;
import ba.maloprodaja.sifarnici.artikalkomp.dto.ArtikalKompDTO;
import ba.maloprodaja.sifarnici.artikalkomp.entity.ArtikalKompanija;
import ba.maloprodaja.sifarnici.artikalkomp.repository.ArtikalKompanijeRepository;
import ba.maloprodaja.sifarnici.atribut.entity.ArtikalAtribut;
import ba.maloprodaja.sifarnici.atribut.repository.ArtikalAtributRepository;
import ba.maloprodaja.sifarnici.dobavljac.entity.Dobavljac;
import ba.maloprodaja.sifarnici.dobavljac.repository.DobavljacRepository;
import ba.maloprodaja.sifarnici.grupaartikala.entity.GrupaArtikala;
import ba.maloprodaja.sifarnici.grupaartikala.repository.GrupaArtikalaRepository;
import ba.maloprodaja.sifarnici.proizvodjac.entity.Proizvodjac;
import ba.maloprodaja.sifarnici.proizvodjac.repository.ProizvodjacRepository;
import ba.maloprodaja.sifarnici.velicina.entity.TipVelicina;
import ba.maloprodaja.sifarnici.velicina.repository.TipVelicinaRepository;
import ba.maloprodaja.sifarnici.varijanta.repository.VarijantaArtiklaRepository;
import ba.maloprodaja.sifarnici.varijanta.service.IVarijantaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtikalKompService implements IArtikalKompService {

    private final ArtikalKompanijeRepository artikalKompanijeRepository;
    private final ArtikalAtributRepository artikalAtributRepository;
    private final GrupaArtikalaRepository grupaArtikalaRepository;
    private final ProizvodjacRepository proizvodjacRepository;
    private final DobavljacRepository dobavljacRepository;
    private final TipVelicinaRepository tipVelicinaRepository;
    private final VarijantaArtiklaRepository varijantaArtiklaRepository;
    private final IVarijantaService varijantaService;

    @Override
    public List<ArtikalKompDTO.ListItemDTO> listAll(Long idKompanije) {
        List<ArtikalKompanija> artikli = artikalKompanijeRepository.findByIdKompanijeOrderByNazivAsc(idKompanije);

        List<Long> artikliIds = artikli.stream().map(ArtikalKompanija::getId).toList();

        List<Long> grupaIds = artikli.stream()
                .map(ArtikalKompanija::getIdGrupe).filter(id -> id != null).distinct().toList();
        List<Long> proizvodjacIds = artikli.stream()
                .map(ArtikalKompanija::getIdProizvodjaca).filter(id -> id != null).distinct().toList();
        List<Long> dobavljacIds = artikli.stream()
                .map(ArtikalKompanija::getIdDobavljaca).filter(id -> id != null).distinct().toList();
        List<Long> tipVelicinaIds = artikli.stream()
                .map(ArtikalKompanija::getIdTipaVelicina).filter(id -> id != null).distinct().toList();

        Map<Long, String> grupeNazivi = grupaArtikalaRepository.findAllById(grupaIds).stream()
                .collect(Collectors.toMap(GrupaArtikala::getId, GrupaArtikala::getNaziv));
        Map<Long, String> proizvodjaciNazivi = proizvodjacRepository.findAllById(proizvodjacIds).stream()
                .collect(Collectors.toMap(Proizvodjac::getId, Proizvodjac::getNaziv));
        Map<Long, String> dobavljaciNazivi = dobavljacRepository.findAllById(dobavljacIds).stream()
                .collect(Collectors.toMap(Dobavljac::getId, Dobavljac::getNaziv));
        Map<Long, String> tipoviVelicinaNames = tipVelicinaRepository.findAllById(tipVelicinaIds).stream()
                .collect(Collectors.toMap(TipVelicina::getId, TipVelicina::getNaziv));

        // Batch-load all attribute selections for this company in a single query — no N+1
        Map<Long, Map<Long, Long>> atributiByArtikalId = artikliIds.isEmpty()
                ? Map.of()
                : artikalAtributRepository.findByIdArtiklaInAndIdKompanije(artikliIds, idKompanije)
                        .stream()
                        .collect(Collectors.groupingBy(
                                ArtikalAtribut::getIdArtikla,
                                Collectors.toMap(ArtikalAtribut::getIdDefinicije, ArtikalAtribut::getIdVrijednosti)
                        ));

        return artikli.stream()
                .map(a -> toListItemDTO(a, grupeNazivi, proizvodjaciNazivi, dobavljaciNazivi, tipoviVelicinaNames,
                        atributiByArtikalId.getOrDefault(a.getId(), Map.of())))
                .toList();
    }

    @Override
    @Transactional
    public ArtikalKompDTO.ListItemDTO create(ArtikalKompDTO.CreateDTO dto, Long idKompanije) {
        if (artikalKompanijeRepository.existsBySifraAndIdKompanije(dto.sifra(), idKompanije)) {
            throw new BusinessException("Artikal sa šifrom '" + dto.sifra() + "' već postoji u ovoj kompaniji.");
        }

        validateForeignKeys(dto.idGrupe(), dto.idProizvodjaca(), dto.idDobavljaca());
        validateTipVelicina(dto.idTipaVelicina());

        ArtikalKompanija a = new ArtikalKompanija();
        applyFields(dto.naziv(), dto.sifra(), dto.opis(), dto.jedin(), dto.pdv(),
                dto.idGrupe(), dto.idProizvodjaca(), dto.idDobavljaca(), dto.idTipaVelicina(), a);
        a.setAktivan(true);
        if (dto.popustProcenat() != null) {
            a.setPopustProcenat(dto.popustProcenat());
        }
        a.setIdKompanije(idKompanije);

        ArtikalKompanija saved = artikalKompanijeRepository.save(a);
        log.info("Kreiran novi artikal: sifra='{}', naziv='{}', idKompanije={}",
                saved.getSifra(), saved.getNaziv(), idKompanije);

        varijantaService.kreirajDefaultVarijantu(saved.getId(), idKompanije);

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
        validateTipVelicina(dto.idTipaVelicina());
        validateTipVelicinaChange(a, dto.idTipaVelicina());

        applyFields(dto.naziv(), dto.sifra(), dto.opis(), dto.jedin(), dto.pdv(),
                dto.idGrupe(), dto.idProizvodjaca(), dto.idDobavljaca(), dto.idTipaVelicina(), a);
        if (dto.aktivan() != null) {
            a.setAktivan(dto.aktivan());
        }
        if (dto.popustProcenat() != null) {
            a.setPopustProcenat(dto.popustProcenat());
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
                              Long idTipaVelicina,
                              ArtikalKompanija a) {
        a.setNaziv(naziv);
        a.setSifra(sifra);
        a.setOpis(opis);
        a.setJedin(jedin);
        a.setPdv(pdv);
        a.setIdGrupe(idGrupe);
        a.setIdProizvodjaca(idProizvodjaca);
        a.setIdDobavljaca(idDobavljaca);
        a.setIdTipaVelicina(idTipaVelicina);
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

    private void validateTipVelicina(Long idTipaVelicina) {
        if (idTipaVelicina != null) {
            tipVelicinaRepository.findById(idTipaVelicina)
                    .orElseThrow(() -> new ResourceNotFoundException("TipVelicina", idTipaVelicina));
        }
    }

    private void validateTipVelicinaChange(ArtikalKompanija artikal, Long noviIdTipaVelicina) {
        boolean tipaSeNijeMijenjao = Objects.equals(artikal.getIdTipaVelicina(), noviIdTipaVelicina);
        if (tipaSeNijeMijenjao) {
            return;
        }
        if (varijantaArtiklaRepository.existsActiveWithVelicinaByIdArtikla(artikal.getId())) {
            throw new BusinessException(
                    "Ne možete promijeniti tip veličine jer artikal već ima aktivnih varijanti sa veličinama. " +
                    "Deaktivirajte ili obrišite varijante sa veličinama prije promjene tipa.");
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
        String nazivTipaVelicina = a.getIdTipaVelicina() != null
                ? tipVelicinaRepository.findById(a.getIdTipaVelicina()).map(TipVelicina::getNaziv).orElse(null)
                : null;

        Map<Long, Long> atributi = artikalAtributRepository.findByIdArtikla(a.getId())
                .stream()
                .collect(Collectors.toMap(ArtikalAtribut::getIdDefinicije, ArtikalAtribut::getIdVrijednosti));

        return buildDTO(a, nazivGrupe, nazivProizvodjaca, nazivDobavljaca, nazivTipaVelicina, atributi);
    }

    private ArtikalKompDTO.ListItemDTO toListItemDTO(ArtikalKompanija a,
                                                      Map<Long, String> grupeNazivi,
                                                      Map<Long, String> proizvodjaciNazivi,
                                                      Map<Long, String> dobavljaciNazivi,
                                                      Map<Long, String> tipoviVelicinaNames,
                                                      Map<Long, Long> atributi) {
        return buildDTO(
                a,
                a.getIdGrupe() != null ? grupeNazivi.get(a.getIdGrupe()) : null,
                a.getIdProizvodjaca() != null ? proizvodjaciNazivi.get(a.getIdProizvodjaca()) : null,
                a.getIdDobavljaca() != null ? dobavljaciNazivi.get(a.getIdDobavljaca()) : null,
                a.getIdTipaVelicina() != null ? tipoviVelicinaNames.get(a.getIdTipaVelicina()) : null,
                atributi
        );
    }

    private ArtikalKompDTO.ListItemDTO buildDTO(ArtikalKompanija a,
                                                 String nazivGrupe,
                                                 String nazivProizvodjaca,
                                                 String nazivDobavljaca,
                                                 String nazivTipaVelicina,
                                                 Map<Long, Long> atributi) {
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
                nazivDobavljaca,
                a.getIdTipaVelicina(),
                nazivTipaVelicina,
                atributi,
                a.getPopustProcenat()
        );
    }
}
