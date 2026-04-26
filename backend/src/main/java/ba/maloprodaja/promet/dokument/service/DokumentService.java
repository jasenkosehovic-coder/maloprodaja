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
        dokument.setBrojFakture(dto.brojFakture());
        dokument.setStatus(StatusDokumenta.NACRT);
        dokument.setIdKompanije(idKompanije);

        Dokument saved = dokumentRepository.save(dokument);

        if (dto.stavke() != null && !dto.stavke().isEmpty()) {
            for (StavkaDTO.CreateStavkaDTO stavkaDto : dto.stavke()) {
                addStavkaToDocument(saved, stavkaDto, idKompanije);
            }
            refreshDokumentIznosi(saved);
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
        dokument.setBrojFakture(dto.brojFakture());

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
            stavka.setVpc(orig.getVpc());
            stavka.setMpc(orig.getMpc());
            stavka.setPdvProcenat(orig.getPdvProcenat());
            stavka.setMarzaProcenat(orig.getMarzaProcenat());
            stavka.setPopustProcenat(orig.getPopustProcenat());
            stavka.setIznosVpc(orig.getIznosVpc().negate());
            stavka.setIznosMpc(orig.getIznosMpc().negate());
            stavka.setIznosMarze(orig.getIznosMarze().negate());
            stavka.setIznosPopusta(orig.getIznosPopusta().negate());
            stavka.setIznosPdv(orig.getIznosPdv().negate());
            stavka.setIdKompanije(idKompanije);
            savedStorno.getStavke().add(stavka);
        }

        refreshDokumentIznosi(savedStorno);
        dokumentRepository.save(savedStorno);
        return potvrdi(savedStorno.getId(), idKompanije);
    }

    @Override
    @Transactional
    public DokumentDTO.DokumentResponseDTO addStavka(Long dokumentId, StavkaDTO.CreateStavkaDTO dto, Long idKompanije) {
        Dokument dokument = loadDokument(dokumentId, idKompanije);
        requireStatus(dokument, StatusDokumenta.NACRT, "dodati stavku u");

        addStavkaToDocument(dokument, dto, idKompanije);
        refreshDokumentIznosi(dokument);
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

        StavkaIznosi iznosi = calculateStavkaIznosi(dto.vpc(), dto.marzaProcenat(), dto.popustProcenat(), dto.pdvProcenat(), potpisanaKolicina);

        stavka.setKolicina(potpisanaKolicina);
        stavka.setVpc(dto.vpc());
        stavka.setMarzaProcenat(dto.marzaProcenat());
        stavka.setPopustProcenat(dto.popustProcenat());
        stavka.setPdvProcenat(dto.pdvProcenat());
        stavka.setMpc(iznosi.mpc());
        stavka.setIznosVpc(iznosi.iznosVpc());
        stavka.setIznosMpc(iznosi.iznosMpc());
        stavka.setIznosMarze(iznosi.iznosMarze());
        stavka.setIznosPopusta(iznosi.iznosPopusta());
        stavka.setIznosPdv(iznosi.iznosPdv());
        stavkaRepository.save(stavka);

        Dokument dokument = loadDokumentWithStavke(stavka.getDokument().getId(), idKompanije);
        refreshDokumentIznosi(dokument);
        dokumentRepository.save(dokument);

        return toDokumentResponse(dokument);
    }

    @Override
    @Transactional
    public void deleteStavka(Long stavkaId, Long idKompanije) {
        StavkaDokumenta stavka = stavkaRepository.findById(stavkaId)
                .filter(s -> s.getIdKompanije().equals(idKompanije))
                .orElseThrow(() -> new ResourceNotFoundException("StavkaDokumenta", stavkaId));

        requireStatus(stavka.getDokument(), StatusDokumenta.NACRT, "brisati stavku iz");
        Long dokumentId = stavka.getDokument().getId();
        stavkaRepository.delete(stavka);
        stavkaRepository.flush();

        Dokument dokument = loadDokumentWithStavke(dokumentId, idKompanije);
        refreshDokumentIznosi(dokument);
        dokumentRepository.save(dokument);
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
        refreshDokumentIznosi(savedIzlaz);

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
        refreshDokumentIznosi(savedUlaz);

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
                            stavka.getVpc()
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

        if (stavkaRepository.existsByDokumentIdAndIdVarijante(dokument.getId(), dto.idVarijante())) {
            throw new BusinessException("Varijanta je već dodana u stavke ovog dokumenta.");
        }

        // Korisnik uvijek unosi pozitivnu količinu; servis primjenjuje smjer tipa dokumenta
        BigDecimal potpisanaKolicina = dto.kolicina()
                .multiply(BigDecimal.valueOf(dokument.getTipDokumenta().getSmjerKolicine()));

        StavkaIznosi iznosi = calculateStavkaIznosi(dto.vpc(), dto.marzaProcenat(), dto.popustProcenat(), dto.pdvProcenat(), potpisanaKolicina);

        StavkaDokumenta stavka = new StavkaDokumenta();
        stavka.setDokument(dokument);
        stavka.setIdVarijante(dto.idVarijante());
        stavka.setKolicina(potpisanaKolicina);
        stavka.setVpc(dto.vpc());
        stavka.setMarzaProcenat(dto.marzaProcenat());
        stavka.setPopustProcenat(dto.popustProcenat());
        stavka.setPdvProcenat(dto.pdvProcenat());
        stavka.setMpc(iznosi.mpc());
        stavka.setIznosVpc(iznosi.iznosVpc());
        stavka.setIznosMpc(iznosi.iznosMpc());
        stavka.setIznosMarze(iznosi.iznosMarze());
        stavka.setIznosPopusta(iznosi.iznosPopusta());
        stavka.setIznosPdv(iznosi.iznosPdv());
        stavka.setIdKompanije(idKompanije);
        stavkaRepository.save(stavka);
        dokument.getStavke().add(stavka);
    }

    /**
     * Calculates all financial amounts for a stavka based on the pricing formula.
     *
     * <pre>
     * popust_per_unit = vpc × popust_procenat / 100                        (rabat dobavljača)
     * vpc_neto        = vpc − popust_per_unit                              (bruto VPC sa PDV)
     * vpc_bez_pdv     = vpc_neto × 100 / (100 + pdv_procenat)             (ekstrakcija PDV iz VPC)
     * pdv_per_unit    = vpc_neto − vpc_bez_pdv                            (iznos PDV za prikaz)
     * marza_per_unit  = vpc_neto × marza_procenat / 100                   (marža na bruto VPC)
     * mpc             = (vpc_neto + marza_per_unit) × (1 + pdv/100)       (PDV na ukupnu cijenu)
     * iznos_vpc       = vpc_bez_pdv × |kolicina|
     * </pre>
     */
    private StavkaIznosi calculateStavkaIznosi(
            BigDecimal vpc,
            BigDecimal marzaProcenat,
            BigDecimal popustProcenat,
            BigDecimal pdvProcenat,
            BigDecimal kolicina
    ) {
        BigDecimal sto = BigDecimal.valueOf(100);

        BigDecimal popustPerUnit = vpc.multiply(popustProcenat)
                .divide(sto, 4, RoundingMode.HALF_UP);

        BigDecimal vpcNeto = vpc.subtract(popustPerUnit);  // bruto VPC nakon popusta (sadrži PDV)

        BigDecimal stoPlusPdv = sto.add(pdvProcenat);
        BigDecimal vpcBezPdv = vpcNeto.multiply(sto)
                .divide(stoPlusPdv, 4, RoundingMode.HALF_UP);
        BigDecimal pdvPerUnit = vpcNeto.subtract(vpcBezPdv);  // izvučeni PDV iz VPC (za prikaz)

        BigDecimal marzaPerUnit = vpcNeto.multiply(marzaProcenat)
                .divide(sto, 4, RoundingMode.HALF_UP);

        BigDecimal ukupnaCijena = vpcNeto.add(marzaPerUnit);
        BigDecimal mpc = ukupnaCijena.multiply(sto.add(pdvProcenat))
                .divide(sto, 2, RoundingMode.HALF_UP);

        BigDecimal apsKolicina = kolicina.abs();

        BigDecimal iznosVpc     = vpcBezPdv.multiply(apsKolicina).setScale(4, RoundingMode.HALF_UP);
        BigDecimal iznosMarze   = marzaPerUnit.multiply(apsKolicina).setScale(4, RoundingMode.HALF_UP);
        BigDecimal iznosPopusta = popustPerUnit.multiply(apsKolicina).setScale(4, RoundingMode.HALF_UP);
        BigDecimal iznosPdv     = pdvPerUnit.multiply(apsKolicina).setScale(4, RoundingMode.HALF_UP);
        BigDecimal iznosMpc     = mpc.multiply(apsKolicina).setScale(4, RoundingMode.HALF_UP);

        if (kolicina.signum() < 0) {
            iznosVpc     = iznosVpc.negate();
            iznosMarze   = iznosMarze.negate();
            iznosPopusta = iznosPopusta.negate();
            iznosPdv     = iznosPdv.negate();
            iznosMpc     = iznosMpc.negate();
        }

        return new StavkaIznosi(mpc, iznosVpc, iznosMpc, iznosMarze, iznosPopusta, iznosPdv);
    }

    private record StavkaIznosi(
            BigDecimal mpc,
            BigDecimal iznosVpc,
            BigDecimal iznosMpc,
            BigDecimal iznosMarze,
            BigDecimal iznosPopusta,
            BigDecimal iznosPdv
    ) {}

    private void refreshDokumentIznosi(Dokument d) {
        if (d.getStavke().isEmpty()) {
            d.setIznosMpc(BigDecimal.ZERO);
            d.setIznosVpc(BigDecimal.ZERO);
            d.setIznosPdv(BigDecimal.ZERO);
            d.setIznosPopusta(BigDecimal.ZERO);
            d.setIznosMarze(BigDecimal.ZERO);
            return;
        }

        BigDecimal sumMpc     = BigDecimal.ZERO;
        BigDecimal sumVpc     = BigDecimal.ZERO;
        BigDecimal sumPdv     = BigDecimal.ZERO;
        BigDecimal sumPopusta = BigDecimal.ZERO;
        BigDecimal sumMarze   = BigDecimal.ZERO;

        for (StavkaDokumenta s : d.getStavke()) {
            sumMpc     = sumMpc.add(s.getIznosMpc());
            sumVpc     = sumVpc.add(s.getIznosVpc());
            sumPdv     = sumPdv.add(s.getIznosPdv());
            sumPopusta = sumPopusta.add(s.getIznosPopusta());
            sumMarze   = sumMarze.add(s.getIznosMarze());
        }

        d.setIznosMpc(sumMpc.setScale(4, RoundingMode.HALF_UP));
        d.setIznosVpc(sumVpc.setScale(4, RoundingMode.HALF_UP));
        d.setIznosPdv(sumPdv.setScale(4, RoundingMode.HALF_UP));
        d.setIznosPopusta(sumPopusta.setScale(4, RoundingMode.HALF_UP));
        d.setIznosMarze(sumMarze.setScale(4, RoundingMode.HALF_UP));
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

        Set<Long> varijantaIds = d.getStavke().stream()
                .map(StavkaDokumenta::getIdVarijante)
                .collect(Collectors.toSet());
        Map<Long, VarijantaArtikla> varijanteMap = varijantaArtiklaRepository
                .findAllWithDetailsByIdIn(varijantaIds)
                .stream()
                .collect(Collectors.toMap(VarijantaArtikla::getId, v -> v));

        List<StavkaDTO.StavkaResponseDTO> stavkeDto = d.getStavke().stream()
                .map(s -> toStavkaResponse(s, varijanteMap))
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
                d.getBrojFakture(),
                d.getDatum(),
                d.getNapomena(),
                d.getIznosMpc(),
                d.getIznosVpc(),
                d.getIznosPdv(),
                d.getIznosPopusta(),
                d.getIznosMarze(),
                stavkeDto,
                d.getSysCreatedDate(),
                d.getSysModifiedDate()
        );
    }

    private StavkaDTO.StavkaResponseDTO toStavkaResponse(StavkaDokumenta s, Map<Long, VarijantaArtikla> varijanteMap) {
        String artikalNaziv = null;
        String velicinaOznaka = null;
        String bojaNaziv = null;

        VarijantaArtikla varijanta = varijanteMap.get(s.getIdVarijante());
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
                s.getVpc(),
                s.getMpc(),
                s.getPopustProcenat(),
                s.getPdvProcenat(),
                s.getMarzaProcenat(),
                s.getIznosVpc(),
                s.getIznosMpc(),
                s.getIznosMarze(),
                s.getIznosPopusta(),
                s.getIznosPdv(),
                s.getSysCreatedDate()
        );
    }

    private DokumentDTO.DokumentListItemDTO toDokumentListItem(
            Dokument d,
            Map<Long, String> poslovniceNazivi,
            Map<Long, String> dobavljaciNazivi
    ) {
        return new DokumentDTO.DokumentListItemDTO(
                d.getId(),
                d.getTipDokumenta().getKod(),
                d.getTipDokumenta().getNaziv(),
                d.getIdPoslovnice(),
                poslovniceNazivi.getOrDefault(d.getIdPoslovnice(), ""),
                d.getIdDobavljaca() != null ? dobavljaciNazivi.getOrDefault(d.getIdDobavljaca(), "") : null,
                d.getStatus().name(),
                d.getBrojDokumenta(),
                d.getBrojFakture(),
                d.getDatum(),
                d.getIznosMpc(),
                d.getIznosVpc(),
                d.getSysCreatedDate()
        );
    }
}
