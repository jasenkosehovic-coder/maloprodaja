package ba.maloprodaja.sifarnici.atribut.service;

import ba.maloprodaja.common.exception.BusinessException;
import ba.maloprodaja.common.exception.ResourceNotFoundException;
import ba.maloprodaja.sifarnici.artikalkomp.repository.ArtikalKompanijeRepository;
import ba.maloprodaja.sifarnici.atribut.dto.ArtikalAtributDTO;
import ba.maloprodaja.sifarnici.atribut.dto.DefinicijaAtributaDTO;
import ba.maloprodaja.sifarnici.atribut.dto.VrijednostAtributaDTO;
import ba.maloprodaja.sifarnici.atribut.entity.ArtikalAtribut;
import ba.maloprodaja.sifarnici.atribut.entity.DefinicijaAtributa;
import ba.maloprodaja.sifarnici.atribut.entity.VrijednostAtributa;
import ba.maloprodaja.sifarnici.atribut.repository.ArtikalAtributRepository;
import ba.maloprodaja.sifarnici.atribut.repository.DefinicijaAtributaRepository;
import ba.maloprodaja.sifarnici.atribut.repository.VrijednostAtributaRepository;
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
public class AtributService implements IAtributService {

    private final DefinicijaAtributaRepository definicijaAtributaRepository;
    private final VrijednostAtributaRepository vrijednostAtributaRepository;
    private final ArtikalAtributRepository artikalAtributRepository;
    private final ArtikalKompanijeRepository artikalKompanijeRepository;

    // ---- Definicije ----

    @Override
    public List<DefinicijaAtributaDTO.ListItemDTO> listDefinicije(Long idKompanije) {
        return definicijaAtributaRepository.findByIdKompanijeOrderByRedosljed(idKompanije)
                .stream()
                .map(this::toDefinicijaDTO)
                .toList();
    }

    @Override
    @Transactional
    public DefinicijaAtributaDTO.ListItemDTO createDefinicija(DefinicijaAtributaDTO.CreateDTO dto, Long idKompanije) {
        if (definicijaAtributaRepository.existsByNazivAndIdKompanije(dto.naziv(), idKompanije)) {
            throw new BusinessException("Definicija atributa sa nazivom '" + dto.naziv() + "' već postoji u ovoj kompaniji.");
        }

        DefinicijaAtributa definicija = new DefinicijaAtributa();
        applyDefinicijaFields(dto.naziv(), dto.redosljed(), dto.obavezno(), dto.zaWeb(), definicija);
        definicija.setAktivan(true);
        definicija.setIdKompanije(idKompanije);

        DefinicijaAtributa saved = definicijaAtributaRepository.save(definicija);
        log.info("Kreirana definicija atributa: naziv='{}', idKompanije={}", saved.getNaziv(), idKompanije);
        return toDefinicijaDTO(saved);
    }

    @Override
    @Transactional
    public DefinicijaAtributaDTO.ListItemDTO updateDefinicija(Long id, DefinicijaAtributaDTO.UpdateDTO dto) {
        DefinicijaAtributa definicija = definicijaAtributaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DefinicijaAtributa", id));

        if (!definicija.getNaziv().equals(dto.naziv()) &&
                definicijaAtributaRepository.existsByNazivAndIdKompanijeAndIdNot(dto.naziv(), definicija.getIdKompanije(), id)) {
            throw new BusinessException("Definicija atributa sa nazivom '" + dto.naziv() + "' već postoji u ovoj kompaniji.");
        }

        applyDefinicijaFields(dto.naziv(), dto.redosljed(), dto.obavezno(), dto.zaWeb(), definicija);
        if (dto.aktivan() != null) {
            definicija.setAktivan(dto.aktivan());
        }

        DefinicijaAtributa saved = definicijaAtributaRepository.save(definicija);
        log.info("Ažurirana definicija atributa: id={}", id);
        return toDefinicijaDTO(saved);
    }

    @Override
    @Transactional
    public void deactivateDefinicija(Long id) {
        DefinicijaAtributa definicija = definicijaAtributaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DefinicijaAtributa", id));
        definicija.setAktivan(false);
        definicijaAtributaRepository.save(definicija);
        log.info("Deaktivirana definicija atributa: id={}", id);
    }

    // ---- Vrijednosti ----

    @Override
    public List<VrijednostAtributaDTO.ListItemDTO> listVrijednosti(Long idDefinicije) {
        definicijaAtributaRepository.findById(idDefinicije)
                .orElseThrow(() -> new ResourceNotFoundException("DefinicijaAtributa", idDefinicije));

        return vrijednostAtributaRepository.findByIdDefinicijeOrderByRedosljed(idDefinicije)
                .stream()
                .map(this::toVrijednostDTO)
                .toList();
    }

    @Override
    @Transactional
    public VrijednostAtributaDTO.ListItemDTO addVrijednost(Long idDefinicije, VrijednostAtributaDTO.CreateDTO dto, Long idKompanije) {
        DefinicijaAtributa definicija = definicijaAtributaRepository.findById(idDefinicije)
                .orElseThrow(() -> new ResourceNotFoundException("DefinicijaAtributa", idDefinicije));

        if (!definicija.getIdKompanije().equals(idKompanije)) {
            throw new ResourceNotFoundException("DefinicijaAtributa", idDefinicije);
        }

        VrijednostAtributa vrijednost = new VrijednostAtributa();
        vrijednost.setIdDefinicije(idDefinicije);
        vrijednost.setVrijednost(dto.vrijednost());
        vrijednost.setRedosljed(dto.redosljed());
        vrijednost.setAktivan(true);
        vrijednost.setIdKompanije(idKompanije);

        VrijednostAtributa saved = vrijednostAtributaRepository.save(vrijednost);
        log.info("Dodana vrijednost atributa: vrijednost='{}', idDefinicije={}", saved.getVrijednost(), idDefinicije);
        return toVrijednostDTO(saved);
    }

    @Override
    @Transactional
    public VrijednostAtributaDTO.ListItemDTO updateVrijednost(Long id, VrijednostAtributaDTO.UpdateDTO dto) {
        VrijednostAtributa vrijednost = vrijednostAtributaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VrijednostAtributa", id));

        if (!vrijednost.getVrijednost().equals(dto.vrijednost()) &&
                vrijednostAtributaRepository.existsByIdDefinicijeAndVrijednostAndIdNot(
                        vrijednost.getIdDefinicije(), dto.vrijednost(), id)) {
            throw new BusinessException("Vrijednost '" + dto.vrijednost() + "' već postoji unutar ove definicije atributa.");
        }

        vrijednost.setVrijednost(dto.vrijednost());
        vrijednost.setRedosljed(dto.redosljed());
        if (dto.aktivan() != null) {
            vrijednost.setAktivan(dto.aktivan());
        }

        VrijednostAtributa saved = vrijednostAtributaRepository.save(vrijednost);
        log.info("Ažurirana vrijednost atributa: id={}", id);
        return toVrijednostDTO(saved);
    }

    @Override
    @Transactional
    public void deactivateVrijednost(Long id) {
        VrijednostAtributa vrijednost = vrijednostAtributaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VrijednostAtributa", id));
        vrijednost.setAktivan(false);
        vrijednostAtributaRepository.save(vrijednost);
        log.info("Deaktivirana vrijednost atributa: id={}", id);
    }

    // ---- Artikal atributi ----

    @Override
    public List<ArtikalAtributDTO.ListItemDTO> listArtikalAtributi(Long idArtikla) {
        artikalKompanijeRepository.findById(idArtikla)
                .orElseThrow(() -> new ResourceNotFoundException("ArtikalKompanija", idArtikla));

        List<ArtikalAtribut> atributi = artikalAtributRepository.findByIdArtikla(idArtikla);

        List<Long> definicijeIds = atributi.stream().map(ArtikalAtribut::getIdDefinicije).distinct().toList();
        List<Long> vrijednostiIds = atributi.stream().map(ArtikalAtribut::getIdVrijednosti).distinct().toList();

        Map<Long, String> definicijeNazivi = definicijaAtributaRepository.findAllById(definicijeIds).stream()
                .collect(Collectors.toMap(DefinicijaAtributa::getId, DefinicijaAtributa::getNaziv));
        Map<Long, String> vrijednostiMap = vrijednostAtributaRepository.findAllById(vrijednostiIds).stream()
                .collect(Collectors.toMap(VrijednostAtributa::getId, VrijednostAtributa::getVrijednost));

        return atributi.stream()
                .map(a -> new ArtikalAtributDTO.ListItemDTO(
                        a.getId(),
                        a.getIdDefinicije(),
                        definicijeNazivi.get(a.getIdDefinicije()),
                        a.getIdVrijednosti(),
                        vrijednostiMap.get(a.getIdVrijednosti())
                ))
                .toList();
    }

    @Override
    @Transactional
    public List<ArtikalAtributDTO.ListItemDTO> upsertArtikalAtributi(
            Long idArtikla,
            List<ArtikalAtributDTO.UpsertItemDTO> dtoList,
            Long idKompanije
    ) {
        artikalKompanijeRepository.findById(idArtikla)
                .orElseThrow(() -> new ResourceNotFoundException("ArtikalKompanija", idArtikla));

        for (ArtikalAtributDTO.UpsertItemDTO item : dtoList) {
            validateVrijednostBelongsToDefinicija(item.idDefinicije(), item.idVrijednosti());
        }

        List<Long> incomingDefinicijeIds = dtoList.stream()
                .map(ArtikalAtributDTO.UpsertItemDTO::idDefinicije)
                .distinct()
                .toList();

        if (incomingDefinicijeIds.isEmpty()) {
            artikalAtributRepository.deleteByIdArtikla(idArtikla);
        } else {
            artikalAtributRepository.deleteByIdArtiklaAndIdDefinicijeNotIn(idArtikla, incomingDefinicijeIds);
        }

        for (ArtikalAtributDTO.UpsertItemDTO item : dtoList) {
            ArtikalAtribut existing = artikalAtributRepository
                    .findByIdArtiklaAndIdDefinicije(idArtikla, item.idDefinicije())
                    .orElse(null);

            if (existing != null) {
                existing.setIdVrijednosti(item.idVrijednosti());
                artikalAtributRepository.save(existing);
            } else {
                ArtikalAtribut novi = new ArtikalAtribut();
                novi.setIdArtikla(idArtikla);
                novi.setIdDefinicije(item.idDefinicije());
                novi.setIdVrijednosti(item.idVrijednosti());
                novi.setIdKompanije(idKompanije);
                artikalAtributRepository.save(novi);
            }
        }

        log.info("Upsert atributa za artikal id={}, broj atributa={}", idArtikla, dtoList.size());
        return listArtikalAtributi(idArtikla);
    }

    // ---- Private helpers ----

    private void applyDefinicijaFields(String naziv, int redosljed, boolean obavezno, boolean zaWeb,
                                        DefinicijaAtributa definicija) {
        definicija.setNaziv(naziv);
        definicija.setRedosljed(redosljed);
        definicija.setObavezno(obavezno);
        definicija.setZaWeb(zaWeb);
    }

    private void validateVrijednostBelongsToDefinicija(Long idDefinicije, Long idVrijednosti) {
        definicijaAtributaRepository.findById(idDefinicije)
                .orElseThrow(() -> new ResourceNotFoundException("DefinicijaAtributa", idDefinicije));

        vrijednostAtributaRepository.findByIdAndIdDefinicije(idVrijednosti, idDefinicije)
                .orElseThrow(() -> new BusinessException(
                        "Vrijednost ID=" + idVrijednosti + " ne pripada definiciji ID=" + idDefinicije + "."));
    }

    private DefinicijaAtributaDTO.ListItemDTO toDefinicijaDTO(DefinicijaAtributa d) {
        return new DefinicijaAtributaDTO.ListItemDTO(
                d.getId(),
                d.getNaziv(),
                d.getRedosljed(),
                d.isObavezno(),
                d.isZaWeb(),
                d.isAktivan()
        );
    }

    private VrijednostAtributaDTO.ListItemDTO toVrijednostDTO(VrijednostAtributa v) {
        return new VrijednostAtributaDTO.ListItemDTO(
                v.getId(),
                v.getIdDefinicije(),
                v.getVrijednost(),
                v.getRedosljed(),
                v.isAktivan()
        );
    }
}
