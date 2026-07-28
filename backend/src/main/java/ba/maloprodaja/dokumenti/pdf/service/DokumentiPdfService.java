package ba.maloprodaja.dokumenti.pdf.service;

import ba.maloprodaja.common.exception.BusinessException;
import ba.maloprodaja.common.exception.ResourceNotFoundException;
import ba.maloprodaja.dokumenti.nivelacija.entity.Nivelacija;
import ba.maloprodaja.dokumenti.nivelacija.entity.NivelacijaStavka;
import ba.maloprodaja.dokumenti.nivelacija.repository.NivelacijaRepository;
import ba.maloprodaja.dokumenti.nivelacija.repository.NivelacijaStavkaRepository;
import ba.maloprodaja.kompanija.entity.Kompanija;
import ba.maloprodaja.kompanija.repository.KompanijaRepository;
import ba.maloprodaja.poslovnica.entity.Poslovnica;
import ba.maloprodaja.poslovnica.repository.PoslovnicaRepository;
import ba.maloprodaja.promet.dokument.entity.Dokument;
import ba.maloprodaja.promet.dokument.repository.DokumentRepository;
import ba.maloprodaja.promet.stavka.entity.StavkaDokumenta;
import ba.maloprodaja.promet.stavka.repository.StavkaRepository;
import ba.maloprodaja.sifarnici.artikalkomp.entity.ArtikalKompanija;
import ba.maloprodaja.sifarnici.artikalkomp.repository.ArtikalKompanijeRepository;
import ba.maloprodaja.sifarnici.dobavljac.entity.Dobavljac;
import ba.maloprodaja.sifarnici.dobavljac.repository.DobavljacRepository;
import ba.maloprodaja.sifarnici.varijanta.entity.VarijantaArtikla;
import ba.maloprodaja.sifarnici.varijanta.repository.VarijantaArtiklaRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DokumentiPdfService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final NivelacijaRepository nivelacijaRepository;
    private final NivelacijaStavkaRepository nivelacijaStavkaRepository;
    private final PoslovnicaRepository poslovnicaRepository;
    private final ArtikalKompanijeRepository artikalKompRepository;
    private final DokumentRepository dokumentRepository;
    private final StavkaRepository stavkaRepository;
    private final KompanijaRepository kompanijaRepository;
    private final VarijantaArtiklaRepository varijantaArtiklaRepository;
    private final DobavljacRepository dobavljacRepository;

    public byte[] nivelacijaPdf(Long id) {
        Nivelacija nivelacija = nivelacijaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nivelacija", id));

        List<NivelacijaStavka> stavke = nivelacijaStavkaRepository.findByNivelacijaId(id);

        Map<Long, ArtikalKompanija> artikliMap = artikalKompRepository.findAllById(
                        stavke.stream().map(NivelacijaStavka::getIdArtikla).distinct().toList())
                .stream()
                .collect(Collectors.toMap(ArtikalKompanija::getId, a -> a));

        String nazivPoslovnice = poslovnicaRepository.findById(nivelacija.getIdPoslovnice())
                .map(Poslovnica::getNaziv)
                .orElse("");

        BigDecimal ukupnoNivelacije = stavke.stream()
                .map(NivelacijaStavka::getIznosNivelacije)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<NivelacijaStavkaRedDTO> redovi = stavke.stream()
                .map(s -> {
                    ArtikalKompanija a = artikliMap.get(s.getIdArtikla());
                    return new NivelacijaStavkaRedDTO(
                            a != null ? a.getSifra() : "",
                            a != null ? a.getNaziv() : "",
                            s.getKolicina(),
                            s.getVpcStara(),
                            s.getVpcNova(),
                            s.getMpcStara(),
                            s.getMpcNova(),
                            s.getIznosNivelacije()
                    );
                })
                .toList();

        Map<String, Object> params = new HashMap<>();
        params.put("NASLOV", "NIVELACIJA br: " + nivelacija.getBroj());
        params.put("DATUM", nivelacija.getDatum() != null ? nivelacija.getDatum().toString() : "");
        params.put("VRSTA", nivelacija.getVrsta() != null ? nivelacija.getVrsta().name() : "");
        params.put("POSLOVNICA", nazivPoslovnice);
        params.put("UKUPNO_NIVELACIJE", ukupnoNivelacije);

        JasperReport report = PdfReportBuilder.buildNivelacijaReport();
        return generisiPdf(report, params, redovi);
    }

    // ------------------------------------------------------------------ promet

    public byte[] ulaznaFakturaPdf(Long id, Long idKompanije) {
        Dokument dokument = dokumentRepository.findById(id)
                .filter(d -> d.getIdKompanije().equals(idKompanije))
                .orElseThrow(() -> new ResourceNotFoundException("Dokument", id));

        List<StavkaDokumenta> stavke = stavkaRepository.findByDokumentId(id);

        Map<Long, VarijantaArtikla> varijanteMap = varijantaArtiklaRepository
                .findAllById(stavke.stream().map(StavkaDokumenta::getIdVarijante).distinct().toList())
                .stream()
                .collect(Collectors.toMap(VarijantaArtikla::getId, v -> v));

        Kompanija kompanija = kompanijaRepository.findById(idKompanije)
                .orElseThrow(() -> new ResourceNotFoundException("Kompanija", idKompanije));

        Poslovnica poslovnica = poslovnicaRepository.findById(dokument.getIdPoslovnice())
                .orElseThrow(() -> new ResourceNotFoundException("Poslovnica", dokument.getIdPoslovnice()));

        BufferedImage logoImage = resolveLogoImage(kompanija.getLogo());
        String partner = resolvePartner(dokument);

        Map<String, Object> params = new HashMap<>();
        params.put("KOMPANIJA_NAZIV",   kompanija.getNaziv());
        params.put("KOMPANIJA_ADRESA",  kompanija.getAdresa()  != null ? kompanija.getAdresa()  : "");
        params.put("KOMPANIJA_GRAD",    kompanija.getGrad()    != null ? kompanija.getGrad()    : "");
        params.put("POSLOVNICA_NAZIV",  poslovnica.getNaziv());
        params.put("TIP_NAZIV",         dokument.getTipDokumenta().getNaziv());
        params.put("BROJ_DOKUMENTA",    dokument.getBrojDokumenta() != null ? dokument.getBrojDokumenta() : "");
        params.put("DATUM",             dokument.getDatum() != null ? dokument.getDatum().format(DATE_FORMAT) : "");
        params.put("PARTNER",           partner);
        params.put("NAPOMENA",          dokument.getNapomena() != null ? dokument.getNapomena() : "");
        params.put("BROJ_FAKTURE",      dokument.getBrojFakture() != null ? dokument.getBrojFakture() : "");
        params.put("LOGO_IMAGE",        logoImage);
        params.put("UKUPNO_VPC",        dokument.getIznosVpc().abs());
        params.put("UKUPNO_POPUSTA",    dokument.getIznosPopusta().abs());
        params.put("UKUPNO_MARZE",      dokument.getIznosMarze().abs());
        params.put("UKUPNO_PDV",        dokument.getIznosPdv().abs());
        params.put("UKUPNO_MPC",        dokument.getIznosMpc().abs());

        List<FakturaStavkaRedDTO> redovi = buildFakturaRedovi(stavke, varijanteMap);

        JasperReport report = PdfReportBuilder.buildUlaznaFakturaReport();
        return generisiPdf(report, params, redovi);
    }

    private List<FakturaStavkaRedDTO> buildFakturaRedovi(List<StavkaDokumenta> stavke,
                                                          Map<Long, VarijantaArtikla> varijanteMap) {
        List<FakturaStavkaRedDTO> redovi = new ArrayList<>(stavke.size());
        int rb = 1;
        for (StavkaDokumenta s : stavke) {
            VarijantaArtikla v = varijanteMap.get(s.getIdVarijante());

            String artikalNaziv  = "";
            String varijantaLabel = "";
            if (v != null) {
                if (v.getArtikalKompanija() != null) artikalNaziv = v.getArtikalKompanija().getNaziv();
                String vel = v.getVelicina() != null ? v.getVelicina().getOznaka() : null;
                String boja = v.getBoja()    != null ? v.getBoja().getNaziv()      : null;
                if (vel != null && boja != null)     varijantaLabel = vel + " / " + boja;
                else if (vel != null)                varijantaLabel = vel;
                else if (boja != null)               varijantaLabel = boja;
            }

            redovi.add(new FakturaStavkaRedDTO(
                    rb++,
                    artikalNaziv,
                    varijantaLabel,
                    s.getKolicina().abs(),
                    s.getVpc(),
                    s.getPopustProcenat(),
                    s.getIznosPopusta().abs(),
                    s.getPdvProcenat(),
                    s.getIznosPdv().abs(),
                    s.getMarzaProcenat(),
                    s.getMpc(),
                    s.getIznosMpc().abs()
            ));
        }
        return redovi;
    }

    public byte[] prometDokumentPdf(Long id, Long idKompanije) {
        Dokument dokument = dokumentRepository.findById(id)
                .filter(d -> d.getIdKompanije().equals(idKompanije))
                .orElseThrow(() -> new ResourceNotFoundException("Dokument", id));

        List<StavkaDokumenta> stavke = stavkaRepository.findByDokumentId(id);

        // Batch-load all variants for this document in a single query to avoid N+1
        Map<Long, VarijantaArtikla> varijanteMap = varijantaArtiklaRepository
                .findAllById(stavke.stream().map(StavkaDokumenta::getIdVarijante).distinct().toList())
                .stream()
                .collect(Collectors.toMap(VarijantaArtikla::getId, v -> v));

        Kompanija kompanija = kompanijaRepository.findById(idKompanije)
                .orElseThrow(() -> new ResourceNotFoundException("Kompanija", idKompanije));

        Poslovnica poslovnica = poslovnicaRepository.findById(dokument.getIdPoslovnice())
                .orElseThrow(() -> new ResourceNotFoundException("Poslovnica", dokument.getIdPoslovnice()));

        BufferedImage logoImage = resolveLogoImage(kompanija.getLogo());

        String partner = resolvePartner(dokument);

        BigDecimal ukupno = stavke.stream()
                .map(StavkaDokumenta::getIznosMpc)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .abs();

        List<PrometStavkaRedDTO> redovi = buildRedovi(stavke, varijanteMap);

        Map<String, Object> params = new HashMap<>();
        params.put("KOMPANIJA_NAZIV",   kompanija.getNaziv());
        params.put("KOMPANIJA_ADRESA",  kompanija.getAdresa() != null ? kompanija.getAdresa() : "");
        params.put("KOMPANIJA_GRAD",    kompanija.getGrad()   != null ? kompanija.getGrad()   : "");
        params.put("POSLOVNICA_NAZIV",  poslovnica.getNaziv());
        params.put("TIP_NAZIV",         dokument.getTipDokumenta().getNaziv());
        params.put("BROJ_DOKUMENTA",    dokument.getBrojDokumenta() != null ? dokument.getBrojDokumenta() : "");
        params.put("DATUM",             dokument.getDatum() != null ? dokument.getDatum().format(DATE_FORMAT) : "");
        params.put("PARTNER",           partner);
        params.put("NAPOMENA",          dokument.getNapomena() != null ? dokument.getNapomena() : "");
        params.put("UKUPNO",            ukupno);
        params.put("LOGO_IMAGE",        logoImage); // nullable — JasperReports renders blank when null

        JasperReport report = PdfReportBuilder.buildPrometDokumentReport();
        return generisiPdf(report, params, redovi);
    }

    private List<PrometStavkaRedDTO> buildRedovi(List<StavkaDokumenta> stavke,
                                                  Map<Long, VarijantaArtikla> varijanteMap) {
        List<PrometStavkaRedDTO> redovi = new ArrayList<>(stavke.size());
        int rb = 1;
        for (StavkaDokumenta s : stavke) {
            VarijantaArtikla v = varijanteMap.get(s.getIdVarijante());

            String artikalNaziv = "";
            String varijantaLabel = "";
            if (v != null) {
                if (v.getArtikalKompanija() != null) {
                    artikalNaziv = v.getArtikalKompanija().getNaziv();
                }
                String velicina = v.getVelicina() != null ? v.getVelicina().getOznaka() : null;
                String boja     = v.getBoja()     != null ? v.getBoja().getNaziv()      : null;
                if (velicina != null && boja != null) {
                    varijantaLabel = velicina + " / " + boja;
                } else if (velicina != null) {
                    varijantaLabel = velicina;
                } else if (boja != null) {
                    varijantaLabel = boja;
                }
            }

            redovi.add(new PrometStavkaRedDTO(
                    rb++,
                    artikalNaziv,
                    varijantaLabel,
                    s.getKolicina().abs(),
                    s.getVpc(),
                    s.getPopustProcenat(),
                    s.getIznosMpc().abs()
            ));
        }
        return redovi;
    }

    public byte[] povratDobavljacuPdf(Long id, Long idKompanije) {
        Dokument dokument = dokumentRepository.findById(id)
                .filter(d -> d.getIdKompanije().equals(idKompanije))
                .orElseThrow(() -> new ResourceNotFoundException("Dokument", id));

        List<StavkaDokumenta> stavke = stavkaRepository.findByDokumentId(id);

        Map<Long, VarijantaArtikla> varijanteMap = varijantaArtiklaRepository
                .findAllById(stavke.stream().map(StavkaDokumenta::getIdVarijante).distinct().toList())
                .stream()
                .collect(Collectors.toMap(VarijantaArtikla::getId, v -> v));

        Kompanija kompanija = kompanijaRepository.findById(idKompanije)
                .orElseThrow(() -> new ResourceNotFoundException("Kompanija", idKompanije));

        Poslovnica poslovnica = poslovnicaRepository.findById(dokument.getIdPoslovnice())
                .orElseThrow(() -> new ResourceNotFoundException("Poslovnica", dokument.getIdPoslovnice()));

        BufferedImage logoImage = resolveLogoImage(kompanija.getLogo());
        String partner = resolvePartner(dokument);

        Map<String, Object> params = new HashMap<>();
        params.put("KOMPANIJA_NAZIV",  kompanija.getNaziv());
        params.put("KOMPANIJA_ADRESA", kompanija.getAdresa()  != null ? kompanija.getAdresa()  : "");
        params.put("KOMPANIJA_GRAD",   kompanija.getGrad()    != null ? kompanija.getGrad()    : "");
        params.put("POSLOVNICA_NAZIV", poslovnica.getNaziv());
        params.put("TIP_NAZIV",        dokument.getTipDokumenta().getNaziv());
        params.put("BROJ_DOKUMENTA",   dokument.getBrojDokumenta() != null ? dokument.getBrojDokumenta() : "");
        params.put("DATUM",            dokument.getDatum() != null ? dokument.getDatum().format(DATE_FORMAT) : "");
        params.put("PARTNER",          partner);
        params.put("NAPOMENA",         dokument.getNapomena() != null ? dokument.getNapomena() : "");
        params.put("LOGO_IMAGE",       logoImage);
        BigDecimal ukupnoVpc = dokument.getIznosVpc().abs();
        BigDecimal ukupnoPdv = dokument.getIznosPdv().abs();
        params.put("UKUPNO_VPC", ukupnoVpc);
        params.put("UKUPNO_PDV", ukupnoPdv);
        params.put("UKUPNO",     ukupnoVpc.add(ukupnoPdv));

        List<PovratStavkaRedDTO> redovi = buildPovratRedovi(stavke, varijanteMap);

        JasperReport report = PdfReportBuilder.buildPovratDobavljacuReport();
        return generisiPdf(report, params, redovi);
    }

    private List<PovratStavkaRedDTO> buildPovratRedovi(List<StavkaDokumenta> stavke,
                                                        Map<Long, VarijantaArtikla> varijanteMap) {
        List<PovratStavkaRedDTO> redovi = new ArrayList<>(stavke.size());
        int rb = 1;
        for (StavkaDokumenta s : stavke) {
            VarijantaArtikla v = varijanteMap.get(s.getIdVarijante());

            String artikalNaziv = "";
            String varijantaLabel = "";
            if (v != null) {
                if (v.getArtikalKompanija() != null) artikalNaziv = v.getArtikalKompanija().getNaziv();
                String vel  = v.getVelicina() != null ? v.getVelicina().getOznaka() : null;
                String boja = v.getBoja()     != null ? v.getBoja().getNaziv()      : null;
                if (vel != null && boja != null)     varijantaLabel = vel + " / " + boja;
                else if (vel != null)                varijantaLabel = vel;
                else if (boja != null)               varijantaLabel = boja;
            }

            BigDecimal netVpc   = s.getIznosVpc().abs();
            BigDecimal netPdv   = s.getIznosPdv().abs();
            redovi.add(new PovratStavkaRedDTO(
                    rb++,
                    artikalNaziv,
                    varijantaLabel,
                    s.getKolicina().abs(),
                    s.getVpc(),
                    s.getPdvProcenat(),
                    netPdv,
                    netVpc,
                    netVpc.add(netPdv)
            ));
        }
        return redovi;
    }

    private String resolvePartner(Dokument dokument) {
        if (dokument.getIdDobavljaca() != null) {
            return dobavljacRepository.findById(dokument.getIdDobavljaca())
                    .map(Dobavljac::getNaziv)
                    .orElse("");
        }
        if (dokument.getIdKupca() != null) {
            // Kupac module is planned for future — return empty for now
            return "";
        }
        String tipKod = dokument.getTipDokumenta().getKod();
        return switch (tipKod) {
            case "MSI" -> "Međuskladišnica (izlaz)";
            case "MSU" -> "Međuskladišnica (ulaz)";
            default    -> "";
        };
    }

    /**
     * Converts the raw BYTEA logo stored on Kompanija into a BufferedImage.
     * Returns null (logo is optional) when the byte array is null, empty, or unreadable.
     */
    private BufferedImage resolveLogoImage(byte[] logoBytes) {
        if (logoBytes == null || logoBytes.length == 0) {
            return null;
        }
        try {
            return ImageIO.read(new ByteArrayInputStream(logoBytes));
        } catch (Exception e) {
            log.warn("Logo nije mogao biti učitan iz baze — PDF će biti generisan bez loga: {}", e.getMessage());
            return null;
        }
    }

    private <T> byte[] generisiPdf(JasperReport report, Map<String, Object> params, List<T> redovi) {
        try {
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(redovi);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, params, dataSource);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

            SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
            exporter.setConfiguration(configuration);
            exporter.exportReport();

            return outputStream.toByteArray();

        } catch (JRException e) {
            log.error("Greška pri generisanju PDF-a: {}", e.getMessage(), e);
            throw new BusinessException("Greška pri generisanju PDF izvještaja");
        }
    }

    // Namjerno klase umjesto record — JasperReports (Commons BeanUtils) traži JavaBean getere (getSifra())
    @Getter
    @AllArgsConstructor
    public static class NivelacijaStavkaRedDTO {
        private String sifra;
        private String naziv;
        private BigDecimal kolicina;
        private BigDecimal vpcStara;
        private BigDecimal vpcNova;
        private BigDecimal mpcStara;
        private BigDecimal mpcNova;
        private BigDecimal iznosNivelacije;
    }

    @Getter
    @AllArgsConstructor
    public static class FakturaStavkaRedDTO {
        private int        rb;
        private String     artikal;
        private String     varijanta;
        private BigDecimal kolicina;
        private BigDecimal vpc;
        private BigDecimal popustProcenat;
        private BigDecimal iznosPopusta;
        private BigDecimal pdvProcenat;
        private BigDecimal iznosPdv;
        private BigDecimal marzaProcenat;
        private BigDecimal mpc;
        private BigDecimal iznosMpc;
    }

    @Getter
    @AllArgsConstructor
    public static class PovratStavkaRedDTO {
        private int        rb;
        private String     artikal;
        private String     varijanta;
        private BigDecimal kolicina;
        private BigDecimal vpc;
        private BigDecimal pdvProcenat;
        private BigDecimal iznosPdv;
        private BigDecimal iznosVpc;
        private BigDecimal ukupno;
    }

    /**
     * Row DTO for promet document PDF.
     * Uses a class (not a record) because JasperReports relies on Commons BeanUtils
     * which requires standard JavaBean getters (getRb(), getArtikl(), ...).
     */
    @Getter
    @AllArgsConstructor
    public static class PrometStavkaRedDTO {
        /** 1-based row number. */
        private int rb;
        /** Article name from ArtikalKompanija. */
        private String artikal;
        /** Variant label e.g. "M / Plava", "XL", "Crvena", or "" for default (no size/colour). */
        private String varijanta;
        /** Absolute value of kolicina (sign is stripped — sign lives on tipDokumenta). */
        private BigDecimal kolicina;
        private BigDecimal cijena;
        private BigDecimal popust;
        /** Absolute value of ukupno. */
        private BigDecimal ukupno;
    }
}
