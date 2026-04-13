package ba.maloprodaja.sifarnici.varijanta.service;

import ba.maloprodaja.common.exception.BusinessException;
import ba.maloprodaja.common.exception.ResourceNotFoundException;
import ba.maloprodaja.poslovnica.entity.Poslovnica;
import ba.maloprodaja.poslovnica.repository.PoslovnicaRepository;
import ba.maloprodaja.sifarnici.artikalkomp.entity.ArtikalKompanija;
import ba.maloprodaja.sifarnici.artikalkomp.repository.ArtikalKompanijeRepository;
import ba.maloprodaja.sifarnici.artikalkomp.repository.BarkodRepository;
import ba.maloprodaja.sifarnici.boja.entity.Boja;
import ba.maloprodaja.sifarnici.boja.repository.BojaRepository;
import ba.maloprodaja.sifarnici.velicina.entity.Velicina;
import ba.maloprodaja.sifarnici.velicina.repository.VelicinaRepository;
import ba.maloprodaja.sifarnici.varijanta.dto.VarijantaDTO;
import ba.maloprodaja.sifarnici.varijanta.entity.VarijantaArtikla;
import ba.maloprodaja.sifarnici.varijanta.entity.VarijantaArtiklaPoslovnica;
import ba.maloprodaja.sifarnici.varijanta.repository.VarijantaArtiklaRepository;
import ba.maloprodaja.sifarnici.varijanta.repository.VarijantaArtiklaPoslovnicaRepository;
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
public class VarijantaService implements IVarijantaService {

    private final VarijantaArtiklaRepository varijantaArtiklaRepository;
    private final VarijantaArtiklaPoslovnicaRepository varijantaPoslovnicaRepository;
    private final ArtikalKompanijeRepository artikalKompanijeRepository;
    private final VelicinaRepository velicinaRepository;
    private final BojaRepository bojaRepository;
    private final PoslovnicaRepository poslovnicaRepository;
    private final BarkodRepository barkodRepository;

    @Override
    public List<VarijantaDTO.ListItemDTO> listByArtikl(Long idArtikla, Long idKompanije) {
        validateArtikalBelongsToKompanija(idArtikla, idKompanije);

        List<VarijantaArtikla> varijante = varijantaArtiklaRepository.findByIdArtikla(idArtikla);

        Map<Long, Velicina> velicineMap = buildVelicineMap(varijante);
        Map<Long, Boja> bojeMap = buildBojeMap(varijante);

        List<Long> varijantaIds = varijante.stream()
                .map(VarijantaArtikla::getId)
                .toList();

        Map<Long, List<VarijantaDTO.BarkodInfo>> barkodoviPoVarijanti = barkodRepository.findByIdVarijanteIn(varijantaIds)
                .stream()
                .collect(Collectors.groupingBy(
                        ba.maloprodaja.sifarnici.artikalkomp.entity.Barkod::getIdVarijante,
                        Collectors.mapping(
                                b -> new VarijantaDTO.BarkodInfo(b.getId(), b.getBarkod(), b.isAktivan()),
                                Collectors.toList()
                        )
                ));

        return varijante.stream()
                .map(v -> toListItemDTO(v, velicineMap, bojeMap, barkodoviPoVarijanti.getOrDefault(v.getId(), List.of())))
                .toList();
    }

    @Override
    @Transactional
    public VarijantaDTO.ListItemDTO create(Long idArtikla, VarijantaDTO.CreateDTO dto, Long idKompanije) {
        validateArtikalBelongsToKompanija(idArtikla, idKompanije);
        validateNoDuplicate(idArtikla, dto.idVelicine(), dto.idBoje());

        if (dto.idVelicine() != null) {
            velicinaRepository.findById(dto.idVelicine())
                    .orElseThrow(() -> new ResourceNotFoundException("Velicina", dto.idVelicine()));
        }

        if (dto.idBoje() != null) {
            Boja boja = bojaRepository.findById(dto.idBoje())
                    .orElseThrow(() -> new ResourceNotFoundException("Boja", dto.idBoje()));
            if (!boja.getIdKompanije().equals(idKompanije)) {
                throw new ResourceNotFoundException("Boja", dto.idBoje());
            }
        }

        VarijantaArtikla varijanta = new VarijantaArtikla();
        varijanta.setIdArtikla(idArtikla);
        varijanta.setIdVelicine(dto.idVelicine());
        varijanta.setIdBoje(dto.idBoje());
        varijanta.setAktivan(true);
        varijanta.setIdKompanije(idKompanije);

        VarijantaArtikla saved = varijantaArtiklaRepository.save(varijanta);
        log.info("Kreirana varijanta artikla: idArtikla={}, idVelicine={}, idBoje={}, idKompanije={}",
                idArtikla, dto.idVelicine(), dto.idBoje(), idKompanije);

        kreirajZapisePoPoslovnicama(saved, idKompanije);

        String oznakaVelicine = resolveOznakaVelicine(dto.idVelicine());
        BojaInfo bojaInfo = resolveBojaInfo(dto.idBoje());
        String nazivVarijante = buildNazivVarijante(oznakaVelicine, bojaInfo.naziv());

        return new VarijantaDTO.ListItemDTO(
                saved.getId(), idArtikla,
                dto.idVelicine(), oznakaVelicine,
                dto.idBoje(), bojaInfo.naziv(), bojaInfo.hexKod(),
                nazivVarijante, List.of(), true);
    }

    @Override
    @Transactional
    public VarijantaDTO.ListItemDTO updateAktivan(Long id, VarijantaDTO.UpdateAktivanDTO dto, Long idKompanije) {
        VarijantaArtikla varijanta = varijantaArtiklaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VarijantaArtikla", id));

        if (!dto.aktivan() && varijanta.getIdVelicine() == null && varijanta.getIdBoje() == null) {
            long ukupnoVarijanti = varijantaArtiklaRepository.countByIdArtikla(varijanta.getIdArtikla());
            if (ukupnoVarijanti <= 1) {
                throw new BusinessException(
                        "Ne možete deaktivirati zadnju (default) varijantu artikla.");
            }
        }

        varijanta.setAktivan(dto.aktivan());
        VarijantaArtikla saved = varijantaArtiklaRepository.save(varijanta);
        log.info("Ažuriran status varijante: id={}, aktivan={}", id, dto.aktivan());

        String oznakaVelicine = resolveOznakaVelicine(saved.getIdVelicine());
        BojaInfo bojaInfo = resolveBojaInfo(saved.getIdBoje());
        String nazivVarijante = buildNazivVarijante(oznakaVelicine, bojaInfo.naziv());

        return new VarijantaDTO.ListItemDTO(
                saved.getId(), saved.getIdArtikla(),
                saved.getIdVelicine(), oznakaVelicine,
                saved.getIdBoje(), bojaInfo.naziv(), bojaInfo.hexKod(),
                nazivVarijante, List.of(), saved.isAktivan());
    }

    @Override
    public List<VarijantaDTO.StanjeVarijanteDTO> stanjeByArtikl(Long idArtikla, Long idKompanije) {
        validateArtikalBelongsToKompanija(idArtikla, idKompanije);

        List<VarijantaArtikla> varijante = varijantaArtiklaRepository.findByIdArtikla(idArtikla);

        Map<Long, Velicina> velicineMap = buildVelicineMap(varijante);
        Map<Long, Boja> bojeMap = buildBojeMap(varijante);

        Map<Long, String> poslovniceNazivi = poslovnicaRepository.findByIdKompanije(idKompanije).stream()
                .collect(Collectors.toMap(Poslovnica::getId, Poslovnica::getNaziv));

        return varijante.stream()
                .map(v -> buildStanjeVarijanteDTO(v, velicineMap, bojeMap, poslovniceNazivi))
                .toList();
    }

    @Override
    public List<VarijantaDTO.StanjePoslovniceDTO> stanjeByVarijanta(Long idVarijante, Long idKompanije) {
        VarijantaArtikla varijanta = varijantaArtiklaRepository.findById(idVarijante)
                .orElseThrow(() -> new ResourceNotFoundException("VarijantaArtikla", idVarijante));

        Map<Long, String> poslovniceNazivi = poslovnicaRepository.findByIdKompanije(idKompanije).stream()
                .collect(Collectors.toMap(Poslovnica::getId, Poslovnica::getNaziv));

        return varijantaPoslovnicaRepository.findByIdVarijante(varijanta.getId()).stream()
                .map(vp -> toStanjePoslovniceDTO(vp, poslovniceNazivi))
                .toList();
    }

    @Override
    @Transactional
    public VarijantaDTO.StanjePoslovniceDTO updateStanje(Long id, VarijantaDTO.UpdateStanjeDTO dto, Long idKompanije) {
        VarijantaArtiklaPoslovnica vp = varijantaPoslovnicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VarijantaArtiklaPoslovnica", id));

        vp.setKolicina(dto.kolicina());
        vp.setMinZaliha(dto.minZaliha());
        vp.setOptimalnaZaliha(dto.optimalnaZaliha());

        VarijantaArtiklaPoslovnica saved = varijantaPoslovnicaRepository.save(vp);
        log.info("Ažurirano stanje zalihe: id={}, kolicina={}", id, dto.kolicina());

        Map<Long, String> poslovniceNazivi = poslovnicaRepository.findByIdKompanije(idKompanije).stream()
                .collect(Collectors.toMap(Poslovnica::getId, Poslovnica::getNaziv));

        return toStanjePoslovniceDTO(saved, poslovniceNazivi);
    }

    @Override
    @Transactional
    public VarijantaDTO.StanjePoslovniceDTO updateZalihe(Long id, VarijantaDTO.UpdateZaliheDTO dto, Long idKompanije) {
        VarijantaArtiklaPoslovnica vp = varijantaPoslovnicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VarijantaArtiklaPoslovnica", id));

        vp.setMinZaliha(dto.minZaliha());
        vp.setOptimalnaZaliha(dto.optimalnaZaliha());

        VarijantaArtiklaPoslovnica saved = varijantaPoslovnicaRepository.save(vp);
        log.info("Ažurirane zalihe: id={}, minZaliha={}, optimalnaZaliha={}", id, dto.minZaliha(), dto.optimalnaZaliha());

        Map<Long, String> poslovniceNazivi = poslovnicaRepository.findByIdKompanije(idKompanije).stream()
                .collect(Collectors.toMap(Poslovnica::getId, Poslovnica::getNaziv));

        return toStanjePoslovniceDTO(saved, poslovniceNazivi);
    }

    @Override
    public List<VarijantaDTO.ZalihaListItemDTO> listZaliheByPoslovnica(Long idPoslovnice, Long idKompanije) {
        List<VarijantaArtiklaPoslovnica> zapisi =
                varijantaPoslovnicaRepository.findByIdPoslovniceAndIdKompanije(idPoslovnice, idKompanije);

        // Bulk load varijante
        List<Long> varijantaIds = zapisi.stream().map(VarijantaArtiklaPoslovnica::getIdVarijante).distinct().toList();
        Map<Long, VarijantaArtikla> varijantaMap = varijantaArtiklaRepository.findAllById(varijantaIds)
                .stream().collect(Collectors.toMap(VarijantaArtikla::getId, v -> v));

        // Bulk load artikli
        List<Long> artikalIds = varijantaMap.values().stream().map(VarijantaArtikla::getIdArtikla).distinct().toList();
        Map<Long, ArtikalKompanija> artikalMap = artikalKompanijeRepository.findAllById(artikalIds)
                .stream().collect(Collectors.toMap(ArtikalKompanija::getId, a -> a));

        Map<Long, Velicina> velicineMap = buildVelicineMap(new java.util.ArrayList<>(varijantaMap.values()));
        Map<Long, Boja> bojeMap = buildBojeMap(new java.util.ArrayList<>(varijantaMap.values()));

        return zapisi.stream().map(vp -> {
            VarijantaArtikla v = varijantaMap.get(vp.getIdVarijante());
            if (v == null) return null;
            ArtikalKompanija a = artikalMap.get(v.getIdArtikla());
            if (a == null) return null;

            String oznakaVelicine = v.getIdVelicine() != null
                    ? velicineMap.getOrDefault(v.getIdVelicine(), null) != null
                        ? velicineMap.get(v.getIdVelicine()).getOznaka() : "N/A"
                    : null;
            String nazivBoje = v.getIdBoje() != null
                    ? bojeMap.getOrDefault(v.getIdBoje(), null) != null
                        ? bojeMap.get(v.getIdBoje()).getNaziv() : "N/A"
                    : null;
            String varijantaNaziv = buildNazivVarijante(oznakaVelicine, nazivBoje);

            return new VarijantaDTO.ZalihaListItemDTO(
                    vp.getId(),
                    a.getId(),
                    a.getNaziv(),
                    a.getSifra(),
                    v.getId(),
                    varijantaNaziv,
                    vp.getKolicina(),
                    vp.getMinZaliha(),
                    vp.getOptimalnaZaliha()
            );
        }).filter(dto -> dto != null).toList();
    }

    // ---- Package-accessible helpers used by ArtikalKompService ----

    @Transactional
    public VarijantaArtikla kreirajDefaultVarijantu(Long idArtikla, Long idKompanije) {
        VarijantaArtikla varijanta = new VarijantaArtikla();
        varijanta.setIdArtikla(idArtikla);
        varijanta.setIdVelicine(null);
        varijanta.setIdBoje(null);
        varijanta.setAktivan(true);
        varijanta.setIdKompanije(idKompanije);

        VarijantaArtikla saved = varijantaArtiklaRepository.save(varijanta);
        log.info("Kreirana default varijanta za artikal: idArtikla={}, idKompanije={}",
                idArtikla, idKompanije);

        kreirajZapisePoPoslovnicama(saved, idKompanije);
        return saved;
    }

    // ---- Private helpers ----

    private void validateArtikalBelongsToKompanija(Long idArtikla, Long idKompanije) {
        ArtikalKompanija artikal = artikalKompanijeRepository.findById(idArtikla)
                .orElseThrow(() -> new ResourceNotFoundException("ArtikalKompanija", idArtikla));
        if (!artikal.getIdKompanije().equals(idKompanije)) {
            throw new ResourceNotFoundException("ArtikalKompanija", idArtikla);
        }
    }

    private void validateNoDuplicate(Long idArtikla, Long idVelicine, Long idBoje) {
        if (idVelicine == null && idBoje == null) {
            if (varijantaArtiklaRepository.existsDefaultByIdArtikla(idArtikla)) {
                throw new BusinessException(
                        "Default varijanta (bez veličine i boje) za ovaj artikal već postoji.");
            }
        } else {
            if (varijantaArtiklaRepository.existsByIdArtiklaAndIdVelicinaAndIdBoje(idArtikla, idVelicine, idBoje)) {
                throw new BusinessException(
                        "Varijanta sa ovom kombinacijom veličine i boje za ovaj artikal već postoji.");
            }
        }
    }

    private void kreirajZapisePoPoslovnicama(VarijantaArtikla varijanta, Long idKompanije) {
        List<Poslovnica> poslovnice = poslovnicaRepository.findByIdKompanije(idKompanije);
        List<VarijantaArtiklaPoslovnica> zapisi = poslovnice.stream()
                .map(p -> buildVarijantaPoslovnica(varijanta, p, idKompanije))
                .toList();
        varijantaPoslovnicaRepository.saveAll(zapisi);
        log.info("Kreirani zapisi po {} poslovnica za varijantu id={}", zapisi.size(), varijanta.getId());
    }

    private VarijantaArtiklaPoslovnica buildVarijantaPoslovnica(VarijantaArtikla varijanta,
                                                                  Poslovnica poslovnica,
                                                                  Long idKompanije) {
        VarijantaArtiklaPoslovnica vp = new VarijantaArtiklaPoslovnica();
        vp.setIdVarijante(varijanta.getId());
        vp.setIdPoslovnice(poslovnica.getId());
        vp.setKolicina(BigDecimal.ZERO);
        vp.setIdKompanije(idKompanije);
        return vp;
    }

    private VarijantaDTO.StanjeVarijanteDTO buildStanjeVarijanteDTO(VarijantaArtikla varijanta,
                                                                      Map<Long, Velicina> velicineMap,
                                                                      Map<Long, Boja> bojeMap,
                                                                      Map<Long, String> poslovniceNazivi) {
        String oznakaVelicine = varijanta.getIdVelicine() != null
                ? velicineMap.getOrDefault(varijanta.getIdVelicine(), null) != null
                    ? velicineMap.get(varijanta.getIdVelicine()).getOznaka()
                    : "N/A"
                : null;
        String nazivBoje = varijanta.getIdBoje() != null
                ? bojeMap.getOrDefault(varijanta.getIdBoje(), null) != null
                    ? bojeMap.get(varijanta.getIdBoje()).getNaziv()
                    : "N/A"
                : null;

        String oznaka = buildNazivVarijante(oznakaVelicine, nazivBoje);

        List<VarijantaDTO.StanjePoslovniceDTO> poslovnice =
                varijantaPoslovnicaRepository.findByIdVarijante(varijanta.getId()).stream()
                        .map(vp -> toStanjePoslovniceDTO(vp, poslovniceNazivi))
                        .toList();

        return new VarijantaDTO.StanjeVarijanteDTO(varijanta.getId(), oznaka, poslovnice);
    }

    private VarijantaDTO.StanjePoslovniceDTO toStanjePoslovniceDTO(VarijantaArtiklaPoslovnica vp,
                                                                     Map<Long, String> poslovniceNazivi) {
        return new VarijantaDTO.StanjePoslovniceDTO(
                vp.getId(),
                vp.getIdPoslovnice(),
                poslovniceNazivi.getOrDefault(vp.getIdPoslovnice(), ""),
                vp.getKolicina(),
                vp.getMinZaliha(),
                vp.getOptimalnaZaliha()
        );
    }

    private VarijantaDTO.ListItemDTO toListItemDTO(VarijantaArtikla v,
                                                    Map<Long, Velicina> velicineMap,
                                                    Map<Long, Boja> bojeMap,
                                                    List<VarijantaDTO.BarkodInfo> barkodovi) {
        String oznakaVelicine = v.getIdVelicine() != null
                ? velicineMap.getOrDefault(v.getIdVelicine(), null) != null
                    ? velicineMap.get(v.getIdVelicine()).getOznaka()
                    : "N/A"
                : null;
        Boja boja = v.getIdBoje() != null ? bojeMap.getOrDefault(v.getIdBoje(), null) : null;
        String nazivBoje = boja != null ? boja.getNaziv() : null;
        String hexKodBoje = boja != null ? boja.getHexKod() : null;
        String nazivVarijante = buildNazivVarijante(oznakaVelicine, nazivBoje);

        return new VarijantaDTO.ListItemDTO(
                v.getId(), v.getIdArtikla(),
                v.getIdVelicine(), oznakaVelicine,
                v.getIdBoje(), nazivBoje, hexKodBoje,
                nazivVarijante, barkodovi, v.isAktivan());
    }

    private Map<Long, Velicina> buildVelicineMap(List<VarijantaArtikla> varijante) {
        List<Long> velicinaIds = varijante.stream()
                .map(VarijantaArtikla::getIdVelicine)
                .filter(id -> id != null)
                .distinct()
                .toList();
        return velicinaRepository.findAllById(velicinaIds).stream()
                .collect(Collectors.toMap(Velicina::getId, v -> v));
    }

    private Map<Long, Boja> buildBojeMap(List<VarijantaArtikla> varijante) {
        List<Long> bojaIds = varijante.stream()
                .map(VarijantaArtikla::getIdBoje)
                .filter(id -> id != null)
                .distinct()
                .toList();
        return bojaRepository.findAllById(bojaIds).stream()
                .collect(Collectors.toMap(Boja::getId, b -> b));
    }

    private String resolveOznakaVelicine(Long idVelicine) {
        if (idVelicine == null) {
            return null;
        }
        return velicinaRepository.findById(idVelicine)
                .map(Velicina::getOznaka)
                .orElse("N/A");
    }

    private BojaInfo resolveBojaInfo(Long idBoje) {
        if (idBoje == null) {
            return new BojaInfo(null, null);
        }
        return bojaRepository.findById(idBoje)
                .map(b -> new BojaInfo(b.getNaziv(), b.getHexKod()))
                .orElse(new BojaInfo(null, null));
    }

    private String buildNazivVarijante(String oznakaVelicine, String nazivBoje) {
        boolean hasVelicina = oznakaVelicine != null && !oznakaVelicine.isBlank();
        boolean hasBoja = nazivBoje != null && !nazivBoje.isBlank();

        if (hasVelicina && hasBoja) {
            return oznakaVelicine + " / " + nazivBoje;
        }
        if (hasVelicina) {
            return oznakaVelicine;
        }
        if (hasBoja) {
            return nazivBoje;
        }
        return "Default";
    }

    private record BojaInfo(String naziv, String hexKod) {}
}
