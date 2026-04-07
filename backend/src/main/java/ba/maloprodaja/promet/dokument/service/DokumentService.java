package ba.maloprodaja.promet.dokument.service;

import ba.maloprodaja.common.dto.PageResponseDTO;
import ba.maloprodaja.common.exception.BusinessException;
import ba.maloprodaja.common.exception.ResourceNotFoundException;
import ba.maloprodaja.dokumenti.enums.VrstaNivelacije;
import ba.maloprodaja.dokumenti.nivelacija.service.INivelacijaService;
import ba.maloprodaja.promet.brojac.service.BrojacService;
import ba.maloprodaja.promet.dokument.dto.DokumentDTO;
import ba.maloprodaja.promet.dokument.entity.Dokument;
import ba.maloprodaja.promet.dokument.entity.StatusDokumenta;
import ba.maloprodaja.promet.dokument.repository.DokumentRepository;
import ba.maloprodaja.promet.stavka.dto.StavkaDTO;
import ba.maloprodaja.promet.stavka.entity.StavkaDokumenta;
import ba.maloprodaja.promet.stavka.repository.StavkaRepository;
import ba.maloprodaja.promet.tipdokumenta.entity.TipDokumenta;
import ba.maloprodaja.promet.tipdokumenta.repository.TipDokumentaRepository;
import ba.maloprodaja.promet.tipdokumenta.dto.TipDokumentaDTO;
import ba.maloprodaja.sifarnici.dobavljac.entity.Dobavljac;
import ba.maloprodaja.sifarnici.dobavljac.repository.DobavljacRepository;
import ba.maloprodaja.sifarnici.varijanta.entity.VarijantaArtikla;
import ba.maloprodaja.sifarnici.varijanta.entity.VarijantaArtiklaPoslovnica;
import ba.maloprodaja.sifarnici.varijanta.repository.VarijantaArtiklaRepository;
import ba.maloprodaja.sifarnici.varijanta.repository.VarijantaArtiklaPoslovnicaRepository;
import ba.maloprodaja.poslovnica.entity.Poslovnica;
import ba.maloprodaja.poslovnica.repository.PoslovnicaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DokumentService implements IDokumentService {

    private final DokumentRepository dokumentRepository;
    private final StavkaRepository stavkaRepository;
    private final TipDokumentaRepository tipDokumentaRepository;
    private final DobavljacRepository dobavljacRepository;
    private final PoslovnicaRepository poslovnicaRepository;
    private final VarijantaArtiklaRepository varijantaArtiklaRepository;
    private final VarijantaArtiklaPoslovnicaRepository varijantaPoslovnicaRepository;
    private final BrojacService brojacService;
    private final INivelacijaService nivelacijaService;

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    @Override
    public PageResponseDTO<DokumentDTO.DokumentListItemDTO> findAll(
            Long idKompanije,
            String tipKod,
            Long idPoslovnice,
            String status,
            LocalDate datumOd,
            LocalDate datumDo,
            Pageable pageable
    ) {
        StatusDokumenta statusEnum = parseStatus(status);

        Page<Dokument> page = dokumentRepository.findByFilter(
                idKompanije, tipKod, idPoslovnice, statusEnum, datumOd, datumDo, pageable);

        List<Long> poslovnicaIds = page.getContent().stream()
                .map(Dokument::getIdPoslovnice).distinct().toList();
        List<Long> dobavljacIds = page.getContent().stream()
                .filter(d -> d.getIdDobavljaca() != null)
                .map(Dokument::getIdDobavljaca).distinct().toList();

        Map<Long, String> poslovniceNazivi = poslovnicaRepository.findAllById(poslovnicaIds)
                .stream().collect(Collectors.toMap(Poslovnica::getId, Poslovnica::getNaziv));
        Map<Long, String> dobavljaciNazivi = dobavljacRepository.findAllById(dobavljacIds)
                .stream().collect(Collectors.toMap(Dobavljac::getId, Dobavljac::getNaziv));

        Page<DokumentDTO.DokumentListItemDTO> mapped = page.map(d -> toDokumentListItem(d, poslovniceNazivi, dobavljaciNazivi));
        return PageResponseDTO.of(mapped);
    }

    @Override
    public DokumentDTO.DokumentResponseDTO findById(Long id, Long idKompanije) {
        Dokument dokument = loadDokument(id, idKompanije);
        return toDokumentResponse(dokument);
    }

    // -------------------------------------------------------------------------
    // Commands
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public DokumentDTO.DokumentResponseDTO create(DokumentDTO.CreateDokumentDTO dto, Long idKompanije) {
        TipDokumenta tip = tipDokumentaRepository.findById(dto.idTipa())
                .filter(t -> t.getIdKompanije().equals(idKompanije))
                .orElseThrow(() -> new ResourceNotFoundException("TipDokumenta", dto.idTipa()));

        Poslovnica poslovnica = poslovnicaRepository.findById(dto.idPoslovnice())
                .orElseThrow(() -> new ResourceNotFoundException("Poslovnica", dto.idPoslovnice()));

        if (dto.idDobavljaca() != null) {
            dobavljacRepository.findById(dto.idDobavljaca())
                    .filter(dob -> dob.getIdKompanije().equals(idKompanije))
                    .orElseThrow(() -> new ResourceNotFoundException("Dobavljac", dto.idDobavljaca()));
        }

        Dokument dokument = new Dokument();
        dokument.setTipDokumenta(tip);
        dokument.setIdPoslovnice(poslovnica.getId());
        dokument.setIdDobavljaca(dto.idDobavljaca());
        dokument.setDatum(dto.datum());
        dokument.setNapomena(dto.napomena());
        dokument.setStatus(StatusDokumenta.NACRT);
        dokument.setIdKompanije(idKompanije);

        Dokument saved = dokumentRepository.save(dokument);

        if (dto.stavke() != null && !dto.stavke().isEmpty()) {
            for (StavkaDTO.CreateStavkaDTO stavkaDto : dto.stavke()) {
                addStavkaToDocument(saved, stavkaDto, idKompanije);
            }
        }

        log.info("Kreiran dokument id={}, tip={}, kompanija={}", saved.getId(), tip.getKod(), idKompanije);
        return toDokumentResponse(saved);
    }

    @Override
    @Transactional
    public DokumentDTO.DokumentResponseDTO update(Long id, DokumentDTO.UpdateDokumentDTO dto, Long idKompanije) {
        Dokument dokument = loadDokument(id, idKompanije);
        requireStatus(dokument, StatusDokumenta.NACRT, "ažurirati");

        if (dto.idDobavljaca() != null) {
            dobavljacRepository.findById(dto.idDobavljaca())
                    .filter(dob -> dob.getIdKompanije().equals(idKompanije))
                    .orElseThrow(() -> new ResourceNotFoundException("Dobavljac", dto.idDobavljaca()));
        }

        dokument.setIdDobavljaca(dto.idDobavljaca());
        dokument.setDatum(dto.datum());
        dokument.setNapomena(dto.napomena());

        return toDokumentResponse(dokumentRepository.save(dokument));
    }

    @Override
    @Transactional
    public DokumentDTO.DokumentResponseDTO potvrdi(Long id, Long idKompanije) {
        Dokument dokument = loadDokumentWithStavke(id, idKompanije);
        requireStatus(dokument, StatusDokumenta.NACRT, "potvrditi");

        if (dokument.getStavke().isEmpty()) {
            throw new BusinessException("Dokument ne može biti potvrđen bez stavki.");
        }

        String brojDokumenta = brojacService.nextBrojDokumenta(dokument.getTipDokumenta(), idKompanije);
        dokument.setBrojDokumenta(brojDokumenta);
        dokument.setStatus(StatusDokumenta.POTVRDEN);

        // Kolicina je već potpisana (+/−) na osnovu tipa dokumenta — direktno se dodaje na zalihu
        for (StavkaDokumenta stavka : dokument.getStavke()) {
            updateStock(stavka.getIdVarijante(), dokument.getIdPoslovnice(), idKompanije, stavka.getKolicina());
        }

        Dokument saved = dokumentRepository.save(dokument);
        log.info("Dokument id={} potvrđen, broj={}", id, brojDokumenta);

        if (dokument.getTipDokumenta().getSmjerKolicine() == 1) {
            triggerAutomatskaNivelacija(saved, idKompanije);
        }

        return toDokumentResponse(saved);
    }

    @Override
    @Transactional
    public DokumentDTO.DokumentResponseDTO storniraj(Long id, Long idKompanije) {
        Dokument original = loadDokumentWithStavke(id, idKompanije);
        requireStatus(original, StatusDokumenta.POTVRDEN, "stornirati");

        original.setStatus(StatusDokumenta.STORNIRAN);
        dokumentRepository.save(original);

        Dokument storno = new Dokument();
        storno.setTipDokumenta(original.getTipDokumenta());
        storno.setIdPoslovnice(original.getIdPoslovnice());
        storno.setIdDobavljaca(original.getIdDobavljaca());
        storno.setIdKupca(original.getIdKupca());
        storno.setDatum(LocalDate.now());
        storno.setNapomena("Storno: " + original.getBrojDokumenta());
        storno.setStatus(StatusDokumenta.NACRT);
        storno.setIdKompanije(idKompanije);

        Dokument savedStorno = dokumentRepository.save(storno);

        // Stavke storno dokumenta imaju negiranu količinu → potvrdi() direktno dodaje na zalihu
        for (StavkaDokumenta orig : original.getStavke()) {
            StavkaDokumenta stavka = new StavkaDokumenta();
            stavka.setDokument(savedStorno);
            stavka.setIdVarijante(orig.getIdVarijante());
            stavka.setKolicina(orig.getKolicina().negate());
            stavka.setCijena(orig.getCijena());
            stavka.setPopust(orig.getPopust());
            stavka.setUkupno(orig.getUkupno().negate());
            stavka.setIdKompanije(idKompanije);
            savedStorno.getStavke().add(stavka);
        }

        dokumentRepository.save(savedStorno);
        return potvrdi(savedStorno.getId(), idKompanije);
    }

    @Override
    @Transactional
    public DokumentDTO.DokumentResponseDTO addStavka(Long dokumentId, StavkaDTO.CreateStavkaDTO dto, Long idKompanije) {
        Dokument dokument = loadDokument(dokumentId, idKompanije);
        requireStatus(dokument, StatusDokumenta.NACRT, "dodati stavku u");

        addStavkaToDocument(dokument, dto, idKompanije);
        return toDokumentResponse(dokumentRepository.save(dokument));
    }

    @Override
    @Transactional
    public DokumentDTO.DokumentResponseDTO updateStavka(Long stavkaId, StavkaDTO.UpdateStavkaDTO dto, Long idKompanije) {
        StavkaDokumenta stavka = stavkaRepository.findById(stavkaId)
                .filter(s -> s.getIdKompanije().equals(idKompanije))
                .orElseThrow(() -> new ResourceNotFoundException("StavkaDokumenta", stavkaId));

        requireStatus(stavka.getDokument(), StatusDokumenta.NACRT, "ažurirati stavku u");

        int smjer = stavka.getDokument().getTipDokumenta().getSmjerKolicine();
        BigDecimal potpisanaKolicina = dto.kolicina().multiply(BigDecimal.valueOf(smjer));

        stavka.setKolicina(potpisanaKolicina);
        stavka.setCijena(dto.cijena());
        stavka.setPopust(dto.popust());
        stavka.setUkupno(calculateUkupno(dto.cijena(), dto.popust(), potpisanaKolicina));
        stavkaRepository.save(stavka);

        return toDokumentResponse(loadDokument(stavka.getDokument().getId(), idKompanije));
    }

    @Override
    @Transactional
    public void deleteStavka(Long stavkaId, Long idKompanije) {
        StavkaDokumenta stavka = stavkaRepository.findById(stavkaId)
                .filter(s -> s.getIdKompanije().equals(idKompanije))
                .orElseThrow(() -> new ResourceNotFoundException("StavkaDokumenta", stavkaId));

        requireStatus(stavka.getDokument(), StatusDokumenta.NACRT, "brisati stavku iz");
        stavkaRepository.delete(stavka);
    }

    @Override
    @Transactional
    public void kreirajMedjuskladisnicu(DokumentDTO.CreateMedjuskladisnicaDTO dto, Long idKompanije) {
        if (dto.izvorPoslovnicaId().equals(dto.odredistePoslovnicaId())) {
            throw new BusinessException("Izvorna i odredišna poslovnica moraju biti različite.");
        }
        if (dto.stavke() == null || dto.stavke().isEmpty()) {
            throw new BusinessException("Međuskladišnica mora sadržavati barem jednu stavku.");
        }

        TipDokumenta msi = tipDokumentaRepository.findByKodAndIdKompanije("MSI", idKompanije)
                .orElseThrow(() -> new ResourceNotFoundException("Tip dokumenta MSI nije pronađen za kompaniju " + idKompanije));
        TipDokumenta msu = tipDokumentaRepository.findByKodAndIdKompanije("MSU", idKompanije)
                .orElseThrow(() -> new ResourceNotFoundException("Tip dokumenta MSU nije pronađen za kompaniju " + idKompanije));

        // Create outbound document (MSI) for source branch
        Dokument izlaz = new Dokument();
        izlaz.setTipDokumenta(msi);
        izlaz.setIdPoslovnice(dto.izvorPoslovnicaId());
        izlaz.setDatum(dto.datum());
        izlaz.setNapomena(dto.napomena());
        izlaz.setStatus(StatusDokumenta.NACRT);
        izlaz.setIdKompanije(idKompanije);
        Dokument savedIzlaz = dokumentRepository.save(izlaz);

        for (StavkaDTO.CreateStavkaDTO stavkaDto : dto.stavke()) {
            addStavkaToDocument(savedIzlaz, stavkaDto, idKompanije);
        }

        // Create inbound document (MSU) for destination branch
        Dokument ulaz = new Dokument();
        ulaz.setTipDokumenta(msu);
        ulaz.setIdPoslovnice(dto.odredistePoslovnicaId());
        ulaz.setDatum(dto.datum());
        ulaz.setNapomena(dto.napomena());
        ulaz.setStatus(StatusDokumenta.NACRT);
        ulaz.setIdKompanije(idKompanije);
        Dokument savedUlaz = dokumentRepository.save(ulaz);

        for (StavkaDTO.CreateStavkaDTO stavkaDto : dto.stavke()) {
            addStavkaToDocument(savedUlaz, stavkaDto, idKompanije);
        }

        // Confirm both atomically — either both succeed or transaction rolls back
        potvrdi(savedIzlaz.getId(), idKompanije);
        potvrdi(savedUlaz.getId(), idKompanije);

        log.info("Kreirana međuskladišnica: MSI id={}, MSU id={}, kompanija={}",
                savedIzlaz.getId(), savedUlaz.getId(), idKompanije);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void triggerAutomatskaNivelacija(Dokument dokument, Long idKompanije) {
        String tipKod = dokument.getTipDokumenta().getKod();

        VrstaNivelacije vrsta = switch (tipKod) {
            case "UF"  -> VrstaNivelacije.AUTOMATSKA_FAKTURA;
            case "MSU" -> VrstaNivelacije.AUTOMATSKA_OTPREMNICA;
            default    -> null;
        };

        if (vrsta == null) {
            return;
        }

        try {
            Set<Long> varijantaIds = dokument.getStavke().stream()
                    .map(StavkaDokumenta::getIdVarijante)
                    .collect(Collectors.toSet());

            Map<Long, Long> varijantaToArtikla = varijantaArtiklaRepository.findAllById(varijantaIds)
                    .stream()
                    .collect(Collectors.toMap(VarijantaArtikla::getId, VarijantaArtikla::getIdArtikla));

            List<INivelacijaService.NivelacijaStavkaInfo> stavkeInfo = dokument.getStavke().stream()
                    .map(stavka -> new INivelacijaService.NivelacijaStavkaInfo(
                            varijantaToArtikla.get(stavka.getIdVarijante()),
                            stavka.getKolicina().abs(),
                            stavka.getCijena()
                    ))
                    .toList();

            nivelacijaService.kreirajAutomatskuZaDokument(
                    dokument.getId(),
                    vrsta,
                    idKompanije,
                    dokument.getIdPoslovnice(),
                    dokument.getNapomena(),
                    stavkeInfo
            );

            log.info("Automatska nivelacija kreirana za dokument id={}, tip={}", dokument.getId(), tipKod);
        } catch (Exception e) {
            log.error("Greška pri kreiranju automatske nivelacije za dokument id={}: {}",
                    dokument.getId(), e.getMessage(), e);
        }
    }

    private void addStavkaToDocument(Dokument dokument, StavkaDTO.CreateStavkaDTO dto, Long idKompanije) {
        varijantaArtiklaRepository.findById(dto.idVarijante())
                .filter(v -> v.getIdKompanije().equals(idKompanije))
                .orElseThrow(() -> new ResourceNotFoundException("VarijantaArtikla", dto.idVarijante()));

        // Korisnik uvijek unosi pozitivnu količinu; servis primjenjuje smjer tipa dokumenta
        BigDecimal potpisanaKolicina = dto.kolicina()
                .multiply(BigDecimal.valueOf(dokument.getTipDokumenta().getSmjerKolicine()));

        StavkaDokumenta stavka = new StavkaDokumenta();
        stavka.setDokument(dokument);
        stavka.setIdVarijante(dto.idVarijante());
        stavka.setKolicina(potpisanaKolicina);
        stavka.setCijena(dto.cijena());
        stavka.setPopust(dto.popust());
        stavka.setUkupno(calculateUkupno(dto.cijena(), dto.popust(), potpisanaKolicina));
        stavka.setIdKompanije(idKompanije);
        dokument.getStavke().add(stavka);
    }

    private void updateStock(Long idVarijante, Long idPoslovnice, Long idKompanije, BigDecimal kolicina) {
        VarijantaArtiklaPoslovnica zaliha = varijantaPoslovnicaRepository
                .findByIdVarijanteAndIdPoslovnice(idVarijante, idPoslovnice)
                .orElseGet(() -> {
                    VarijantaArtiklaPoslovnica nova = new VarijantaArtiklaPoslovnica();
                    nova.setIdVarijante(idVarijante);
                    nova.setIdPoslovnice(idPoslovnice);
                    nova.setKolicina(BigDecimal.ZERO);
                    nova.setIdKompanije(idKompanije);
                    return nova;
                });

        // kolicina je već potpisana (+/−) — direktno se dodaje na snapshot zalihe
        zaliha.setKolicina(zaliha.getKolicina().add(kolicina));
        varijantaPoslovnicaRepository.save(zaliha);

        log.debug("Zaliha ažurirana: varijanta={}, poslovnica={}, delta={}, nova={}",
                idVarijante, idPoslovnice, kolicina, zaliha.getKolicina());
    }

    private BigDecimal calculateUkupno(BigDecimal cijena, BigDecimal popust, BigDecimal kolicina) {
        BigDecimal faktorPopusta = BigDecimal.ONE.subtract(
                popust.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
        return cijena.multiply(faktorPopusta).multiply(kolicina)
                .setScale(4, RoundingMode.HALF_UP);
    }

    private Dokument loadDokument(Long id, Long idKompanije) {
        return dokumentRepository.findById(id)
                .filter(d -> d.getIdKompanije().equals(idKompanije))
                .orElseThrow(() -> new ResourceNotFoundException("Dokument", id));
    }

    private Dokument loadDokumentWithStavke(Long id, Long idKompanije) {
        Dokument dokument = loadDokument(id, idKompanije);
        // Force-load stavke within transaction to avoid lazy-loading issues
        dokument.getStavke().size();
        return dokument;
    }

    private void requireStatus(Dokument dokument, StatusDokumenta required, String action) {
        if (dokument.getStatus() != required) {
            throw new BusinessException(
                    "Dokument mora biti u statusu '%s' da bi se mogao %s. Trenutni status: %s"
                            .formatted(required.name(), action, dokument.getStatus().name()));
        }
    }

    private StatusDokumenta parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return StatusDokumenta.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Nepoznat status dokumenta: " + status);
        }
    }

    // -------------------------------------------------------------------------
    // Mapping
    // -------------------------------------------------------------------------

    private DokumentDTO.DokumentResponseDTO toDokumentResponse(Dokument d) {
        String poslovnicaNaziv = poslovnicaRepository.findById(d.getIdPoslovnice())
                .map(Poslovnica::getNaziv).orElse(null);

        String dobavljacNaziv = d.getIdDobavljaca() != null
                ? dobavljacRepository.findById(d.getIdDobavljaca()).map(Dobavljac::getNaziv).orElse(null)
                : null;

        BigDecimal ukupno = d.getStavke().stream()
                .map(StavkaDokumenta::getUkupno)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<StavkaDTO.StavkaResponseDTO> stavkeDto = d.getStavke().stream()
                .map(this::toStavkaResponse)
                .toList();

        TipDokumenta tip = d.getTipDokumenta();

        return new DokumentDTO.DokumentResponseDTO(
                d.getId(),
                new TipDokumentaDTO(tip.getId(), tip.getKod(), tip.getNaziv(), tip.getSmjerKolicine()),
                d.getIdPoslovnice(),
                poslovnicaNaziv,
                d.getIdDobavljaca(),
                dobavljacNaziv,
                d.getIdKupca(),
                d.getStatus().name(),
                d.getBrojDokumenta(),
                d.getDatum(),
                d.getNapomena(),
                ukupno,
                stavkeDto,
                d.getSysCreatedDate(),
                d.getSysModifiedDate()
        );
    }

    private StavkaDTO.StavkaResponseDTO toStavkaResponse(StavkaDokumenta s) {
        String artikalNaziv = null;
        String velicinaOznaka = null;
        String bojaNaziv = null;

        VarijantaArtikla varijanta = varijantaArtiklaRepository.findById(s.getIdVarijante()).orElse(null);
        if (varijanta != null) {
            if (varijanta.getArtikalKompanija() != null) {
                artikalNaziv = varijanta.getArtikalKompanija().getNaziv();
            }
            if (varijanta.getVelicina() != null) {
                velicinaOznaka = varijanta.getVelicina().getOznaka();
            }
            if (varijanta.getBoja() != null) {
                bojaNaziv = varijanta.getBoja().getNaziv();
            }
        }

        return new StavkaDTO.StavkaResponseDTO(
                s.getId(),
                s.getIdVarijante(),
                artikalNaziv,
                velicinaOznaka,
                bojaNaziv,
                s.getKolicina(),
                s.getCijena(),
                s.getPopust(),
                s.getUkupno(),
                s.getSysCreatedDate()
        );
    }

    private DokumentDTO.DokumentListItemDTO toDokumentListItem(
            Dokument d,
            Map<Long, String> poslovniceNazivi,
            Map<Long, String> dobavljaciNazivi
    ) {
        BigDecimal ukupno = d.getStavke().stream()
                .map(StavkaDokumenta::getUkupno)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DokumentDTO.DokumentListItemDTO(
                d.getId(),
                d.getTipDokumenta().getKod(),
                d.getTipDokumenta().getNaziv(),
                d.getIdPoslovnice(),
                poslovniceNazivi.getOrDefault(d.getIdPoslovnice(), ""),
                d.getIdDobavljaca() != null ? dobavljaciNazivi.getOrDefault(d.getIdDobavljaca(), "") : null,
                d.getStatus().name(),
                d.getBrojDokumenta(),
                d.getDatum(),
                ukupno,
                d.getSysCreatedDate()
        );
    }
}
