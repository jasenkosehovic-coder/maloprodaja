package ba.maloprodaja.dokumenti.pdf.service;

import ba.maloprodaja.common.exception.BusinessException;
import ba.maloprodaja.common.exception.ResourceNotFoundException;
import ba.maloprodaja.dokumenti.faktura.entity.UlaznaFaktura;
import ba.maloprodaja.dokumenti.faktura.entity.UlaznaFakturaStavka;
import ba.maloprodaja.dokumenti.faktura.repository.UlaznaFakturaRepository;
import ba.maloprodaja.dokumenti.faktura.repository.UlaznaFakturaStavkaRepository;
import ba.maloprodaja.dokumenti.nivelacija.entity.Nivelacija;
import ba.maloprodaja.dokumenti.nivelacija.entity.NivelacijaStavka;
import ba.maloprodaja.dokumenti.nivelacija.repository.NivelacijaRepository;
import ba.maloprodaja.dokumenti.nivelacija.repository.NivelacijaStavkaRepository;
import ba.maloprodaja.dokumenti.otpremnica.entity.Otpremnica;
import ba.maloprodaja.dokumenti.otpremnica.entity.OtpremnicaStavka;
import ba.maloprodaja.dokumenti.otpremnica.repository.OtpremnicaRepository;
import ba.maloprodaja.dokumenti.otpremnica.repository.OtpremnicaStavkaRepository;
import ba.maloprodaja.poslovnica.entity.Poslovnica;
import ba.maloprodaja.poslovnica.repository.PoslovnicaRepository;
import ba.maloprodaja.sifarnici.artikalkomp.entity.ArtikalKompanija;
import ba.maloprodaja.sifarnici.artikalkomp.repository.ArtikalKompanijeRepository;
import ba.maloprodaja.sifarnici.dobavljac.entity.Dobavljac;
import ba.maloprodaja.sifarnici.dobavljac.repository.DobavljacRepository;
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

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DokumentiPdfService {

    private final UlaznaFakturaRepository fakturaRepository;
    private final UlaznaFakturaStavkaRepository fakturaStavkaRepository;
    private final NivelacijaRepository nivelacijaRepository;
    private final NivelacijaStavkaRepository nivelacijaStavkaRepository;
    private final OtpremnicaRepository otpremnicaRepository;
    private final OtpremnicaStavkaRepository otpremnicaStavkaRepository;
    private final PoslovnicaRepository poslovnicaRepository;
    private final DobavljacRepository dobavljacRepository;
    private final ArtikalKompanijeRepository artikalKompRepository;

    public byte[] fakturaPdf(Long id) {
        UlaznaFaktura faktura = fakturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UlaznaFaktura", id));

        List<UlaznaFakturaStavka> stavke = fakturaStavkaRepository.findByFakturaId(id);

        Map<Long, ArtikalKompanija> artikliMap = dohvatiArtikle(
                stavke.stream().map(UlaznaFakturaStavka::getIdArtikla).distinct().toList()
        );

        String nazivDobavljaca = dobavljacRepository.findById(faktura.getIdDobavljaca())
                .map(Dobavljac::getNaziv)
                .orElse("");

        List<FakturaStavkaRedDTO> redovi = stavke.stream()
                .map(s -> {
                    ArtikalKompanija a = artikliMap.get(s.getIdArtikla());
                    return new FakturaStavkaRedDTO(
                            a != null ? a.getSifra() : "",
                            a != null ? a.getNaziv() : "",
                            s.getKolicina(),
                            s.getVpc(),
                            s.getPdvStopa().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP),
                            s.getIznosPdv(),
                            s.getUkupno()
                    );
                })
                .toList();

        Map<String, Object> params = new HashMap<>();
        params.put("NASLOV", "ULAZNA FAKTURA br: " + faktura.getBroj());
        params.put("DATUM", faktura.getDatum() != null ? faktura.getDatum().toString() : "");
        params.put("DOBAVLJAC", nazivDobavljaca);
        params.put("UKUPNO_BEZ_PDV", faktura.getUkupnoBezPdv());
        params.put("UKUPNO_PDV", faktura.getUkupnoPdv());
        params.put("UKUPNO", faktura.getUkupno());

        JasperReport report = PdfReportBuilder.buildFakturaReport();
        return generisiPdf(report, params, redovi);
    }

    public byte[] nivelacijaPdf(Long id) {
        Nivelacija nivelacija = nivelacijaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nivelacija", id));

        List<NivelacijaStavka> stavke = nivelacijaStavkaRepository.findByNivelacijaId(id);

        Map<Long, ArtikalKompanija> artikliMap = dohvatiArtikle(
                stavke.stream().map(NivelacijaStavka::getIdArtikla).distinct().toList()
        );

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
                            s.getVpc(),
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

    public byte[] otpremnicaPdf(Long id) {
        Otpremnica otpremnica = otpremnicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Otpremnica", id));

        List<OtpremnicaStavka> stavke = otpremnicaStavkaRepository.findByOtpremnicaId(id);

        Map<Long, ArtikalKompanija> artikliMap = dohvatiArtikle(
                stavke.stream().map(OtpremnicaStavka::getIdArtikla).distinct().toList()
        );

        String posiljalac = poslovnicaRepository.findById(otpremnica.getIdPoslovnicePosiljaoca())
                .map(Poslovnica::getNaziv)
                .orElse("");

        String primalac = poslovnicaRepository.findById(otpremnica.getIdPoslovnicePrimaoca())
                .map(Poslovnica::getNaziv)
                .orElse("");

        List<OtpremnicaStavkaRedDTO> redovi = stavke.stream()
                .map(s -> {
                    ArtikalKompanija a = artikliMap.get(s.getIdArtikla());
                    return new OtpremnicaStavkaRedDTO(
                            a != null ? a.getSifra() : "",
                            a != null ? a.getNaziv() : "",
                            s.getKolicina(),
                            s.getVpcPosiljalac(),
                            s.getMpcPosiljalac()
                    );
                })
                .toList();

        Map<String, Object> params = new HashMap<>();
        params.put("NASLOV", "OTPREMNICA br: " + otpremnica.getBroj());
        params.put("DATUM", otpremnica.getDatum() != null ? otpremnica.getDatum().toString() : "");
        params.put("STATUS", otpremnica.getStatus() != null ? otpremnica.getStatus().name() : "");
        params.put("POSILJALAC", posiljalac);
        params.put("PRIMALAC", primalac);

        JasperReport report = PdfReportBuilder.buildOtpremnicaReport();
        return generisiPdf(report, params, redovi);
    }

    private Map<Long, ArtikalKompanija> dohvatiArtikle(List<Long> ids) {
        return artikalKompRepository.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(ArtikalKompanija::getId, a -> a));
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

    // ---- DTO za redove tabela ----

    public record FakturaStavkaRedDTO(
            String sifra,
            String naziv,
            BigDecimal kolicina,
            BigDecimal vpc,
            BigDecimal pdvStopa,
            BigDecimal iznosPdv,
            BigDecimal ukupno
    ) {}

    public record NivelacijaStavkaRedDTO(
            String sifra,
            String naziv,
            BigDecimal kolicina,
            BigDecimal vpc,
            BigDecimal mpcStara,
            BigDecimal mpcNova,
            BigDecimal iznosNivelacije
    ) {}

    public record OtpremnicaStavkaRedDTO(
            String sifra,
            String naziv,
            BigDecimal kolicina,
            BigDecimal vpc,
            BigDecimal mpc
    ) {}
}
