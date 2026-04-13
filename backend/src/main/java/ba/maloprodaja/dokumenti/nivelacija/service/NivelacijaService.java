package ba.maloprodaja.dokumenti.nivelacija.service;

import ba.maloprodaja.common.exception.BusinessException;
import ba.maloprodaja.common.exception.ResourceNotFoundException;
import ba.maloprodaja.dokumenti.enums.VrstaNivelacije;
import ba.maloprodaja.dokumenti.nivelacija.dto.NivelacijaDTO;
import ba.maloprodaja.dokumenti.nivelacija.entity.Nivelacija;
import ba.maloprodaja.dokumenti.nivelacija.entity.NivelacijaStavka;
import ba.maloprodaja.dokumenti.nivelacija.repository.NivelacijaRepository;
import ba.maloprodaja.dokumenti.nivelacija.repository.NivelacijaStavkaRepository;
import ba.maloprodaja.sifarnici.artikalkomp.entity.ArtikalKompanija;
import ba.maloprodaja.sifarnici.artikalkomp.entity.TipMarze;
import ba.maloprodaja.sifarnici.artikalkomp.repository.ArtikalKompanijeRepository;
import ba.maloprodaja.sifarnici.artikalposl.entity.ArtikalPoslovnica;
import ba.maloprodaja.sifarnici.artikalposl.repository.ArtikalPoslovnicaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NivelacijaService implements INivelacijaService {

    private static final BigDecimal VPC_TOLERANCE = new BigDecimal("0.0001");

    private final NivelacijaRepository nivelacijaRepository;
    private final NivelacijaStavkaRepository stavkaRepository;
    private final ArtikalPoslovnicaRepository artikalPoslovnicaRepository;
    private final ArtikalKompanijeRepository artikalKompRepository;

    @Override
    public List<NivelacijaDTO.ListItemDTO> listAll(Long idKompanije, Long idPoslovnice) {
        List<Nivelacija> nivelacije = nivelacijaRepository.findByIdKompanijeAndIdPoslovnice(idKompanije, idPoslovnice);

        List<Long> nivelacijaIds = nivelacije.stream()
                .map(Nivelacija::getId)
                .toList();

        Map<Long, Long> brojStavkiMap = stavkaRepository.findByNivelacijaIdIn(nivelacijaIds)
                .stream()
                .collect(Collectors.groupingBy(s -> s.getNivelacija().getId(), Collectors.counting()));

        return nivelacije.stream()
                .map(n -> new NivelacijaDTO.ListItemDTO(
                        n.getId(),
                        n.getBroj(),
                        n.getDatum(),
                        n.getVrsta().name(),
                        n.getIdFakture(),
                        n.getIdOtpremnice(),
                        brojStavkiMap.getOrDefault(n.getId(), 0L).intValue()
                ))
                .toList();
    }

    @Override
    public NivelacijaDTO.DetailDTO findById(Long id) {
        Nivelacija nivelacija = nivelacijaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nivelacija", id));

        List<NivelacijaStavka> stavke = stavkaRepository.findByNivelacijaId(id);

        List<Long> artikalIds = stavke.stream()
                .map(NivelacijaStavka::getIdArtikla)
                .distinct()
                .toList();

        Map<Long, ArtikalKompanija> artikliMap = artikalKompRepository.findAllById(artikalIds)
                .stream()
                .collect(Collectors.toMap(ArtikalKompanija::getId, a -> a));

        BigDecimal ukupnoNivelacije = stavke.stream()
                .map(NivelacijaStavka::getIznosNivelacije)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<NivelacijaDTO.StavkaDTO> stavkeDtos = stavke.stream()
                .map(s -> {
                    ArtikalKompanija ak = artikliMap.get(s.getIdArtikla());
                    return new NivelacijaDTO.StavkaDTO(
                            s.getId(),
                            s.getIdArtikla(),
                            ak != null ? ak.getNaziv() : "",
                            ak != null ? ak.getSifra() : "",
                            s.getKolicina(),
                            s.getVpcStara(),
                            s.getVpcNova(),
                            s.getMpcStara(),
                            s.getMpcNova(),
                            s.getIznosNivelacije()
                    );
                })
                .toList();

        return new NivelacijaDTO.DetailDTO(
                nivelacija.getId(),
                nivelacija.getBroj(),
                nivelacija.getDatum(),
                nivelacija.getVrsta().name(),
                nivelacija.getIdFakture(),
                nivelacija.getIdOtpremnice(),
                nivelacija.getNapomena(),
                ukupnoNivelacije,
                stavkeDtos
        );
    }

    @Override
    @Transactional
    public void kreirajAutomatskuZaDokument(Long idDokumenta, VrstaNivelacije vrsta,
                                            Long idKompanije, Long idPoslovnice,
                                            String napomena, List<NivelacijaStavkaInfo> stavkeInfo) {
        List<NivelacijaStavka> nivelacijaStavke = new ArrayList<>();

        for (NivelacijaStavkaInfo info : stavkeInfo) {
            Optional<ArtikalPoslovnica> apOpt = artikalPoslovnicaRepository
                    .findByIdArtiklaAndIdPoslovnice(info.idArtikla(), idPoslovnice);

            if (apOpt.isEmpty()) {
                continue;
            }

            ArtikalPoslovnica ap = apOpt.get();
            BigDecimal razlikaVpc = info.vpc().subtract(ap.getVpc() != null ? ap.getVpc() : BigDecimal.ZERO).abs();

            if (razlikaVpc.compareTo(VPC_TOLERANCE) <= 0) {
                continue;
            }

            BigDecimal mpcStara = ap.getMpc() != null ? ap.getMpc() : BigDecimal.ZERO;
            BigDecimal pdvStopa = dohvatiPdvStopu(ap);
            BigDecimal mpcNova = izracunajMpcNovu(ap, info.vpc(), pdvStopa, mpcStara);

            ap.setVpc(info.vpc());
            ap.setMpc(mpcNova);
            artikalPoslovnicaRepository.save(ap);

            BigDecimal vpcStara = ap.getVpc() != null ? ap.getVpc() : BigDecimal.ZERO;
            BigDecimal vpcNova = info.vpc();
            BigDecimal kolicinaStavke = info.kolicina() != null ? info.kolicina() : BigDecimal.ZERO;
            BigDecimal iznosNivelacije = kolicinaStavke
                    .multiply(mpcNova.subtract(mpcStara))
                    .setScale(4, RoundingMode.HALF_UP);

            nivelacijaStavke.add(buildStavka(
                    info.idArtikla(), kolicinaStavke,
                    vpcStara, vpcNova,
                    mpcStara, mpcNova, iznosNivelacije));
        }

        if (nivelacijaStavke.isEmpty()) {
            log.info("Automatska nivelacija za dokument id={}: nema stavki za nivelaciju (sve iste cijene).", idDokumenta);
            return;
        }

        Nivelacija nivelacija = buildNivelacija(
                generišiBroj(idKompanije, idPoslovnice),
                LocalDate.now(),
                vrsta,
                idKompanije,
                idPoslovnice,
                napomena
        );

        // Čuvamo referencu na izvorni prometni dokument u odgovarajuće polje
        if (vrsta == VrstaNivelacije.AUTOMATSKA_FAKTURA) {
            nivelacija.setIdFakture(idDokumenta);
        } else {
            nivelacija.setIdOtpremnice(idDokumenta);
        }

        Nivelacija saved = nivelacijaRepository.save(nivelacija);
        nivelacijaStavke.forEach(s -> s.setNivelacija(saved));
        stavkaRepository.saveAll(nivelacijaStavke);

        log.info("Kreirana automatska nivelacija za dokument id={}, vrsta={}: broj='{}', stavki={}.",
                idDokumenta, vrsta, saved.getBroj(), nivelacijaStavke.size());
    }

    @Override
    @Transactional
    public NivelacijaDTO.DetailDTO kreirajRucnu(NivelacijaDTO.CreateRucnaDTO dto, Long idKompanije, Long idPoslovnice) {
        if (nivelacijaRepository.existsByIdKompanijeAndIdPoslovniceAndBroj(idKompanije, idPoslovnice, dto.broj())) {
            throw new BusinessException("Nivelacija sa brojem '" + dto.broj() + "' već postoji za ovu poslovnicu.");
        }

        List<NivelacijaStavka> nivelacijaStavke = new ArrayList<>();

        for (NivelacijaDTO.CreateStavkaDTO stavkaDto : dto.stavke()) {
            ArtikalPoslovnica ap = artikalPoslovnicaRepository
                    .findByIdArtiklaAndIdPoslovnice(stavkaDto.idArtikla(), idPoslovnice)
                    .orElseThrow(() -> new BusinessException(
                            "Artikal sa ID=" + stavkaDto.idArtikla() + " nije pronađen na poslovnici."));

            BigDecimal mpcStara = ap.getMpc() != null ? ap.getMpc() : BigDecimal.ZERO;
            BigDecimal mpcNova = stavkaDto.mpcNova();

            ap.setMpc(mpcNova);
            artikalPoslovnicaRepository.save(ap);

            // TODO Sprint 4: prebaciti na varijante_artikla_poslovnica
            BigDecimal iznosNivelacije = BigDecimal.ZERO;
            // BigDecimal iznosNivelacije = ap.getKolicina().multiply(mpcNova.subtract(mpcStara))
            //         .setScale(4, RoundingMode.HALF_UP);

            // Ručna nivelacija mijenja samo MPC — VPC ostaje ista
            BigDecimal vpc = ap.getVpc() != null ? ap.getVpc() : BigDecimal.ZERO;
            nivelacijaStavke.add(buildStavka(stavkaDto.idArtikla(), BigDecimal.ZERO,
                    vpc, vpc, mpcStara, mpcNova, iznosNivelacije));
        }

        Nivelacija nivelacija = buildNivelacija(
                dto.broj(),
                dto.datum(),
                VrstaNivelacije.RUCNA,
                idKompanije,
                idPoslovnice,
                dto.napomena()
        );

        Nivelacija saved = nivelacijaRepository.save(nivelacija);
        nivelacijaStavke.forEach(s -> s.setNivelacija(saved));
        stavkaRepository.saveAll(nivelacijaStavke);

        log.info("Kreirana ručna nivelacija: broj='{}', idKompanije={}, idPoslovnice={}, stavki={}.",
                dto.broj(), idKompanije, idPoslovnice, nivelacijaStavke.size());

        return findById(saved.getId());
    }

    private String generišiBroj(Long idKompanije, Long idPoslovnice) {
        int godina = LocalDate.now().getYear();
        long count = nivelacijaRepository.countByIdKompanijeAndIdPoslovniceAndGodina(idKompanije, idPoslovnice, godina);
        return "NIV-" + godina + "-" + (count + 1);
    }

    private BigDecimal izracunajMpcNovu(ArtikalPoslovnica ap, BigDecimal noviVpc, BigDecimal pdvStopa, BigDecimal mpcStara) {
        return switch (ap.getTipMarze()) {
            case FIKSNA_MARZA -> {
                BigDecimal marza = ap.getMarza() != null ? ap.getMarza() : BigDecimal.ZERO;
                BigDecimal faktorMarze = BigDecimal.ONE.add(marza.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP));
                BigDecimal faktorPdv = BigDecimal.ONE.add(pdvStopa != null ? pdvStopa : BigDecimal.ZERO);
                yield noviVpc.multiply(faktorMarze).multiply(faktorPdv).setScale(4, RoundingMode.HALF_UP);
            }
            case FIKSNA_CIJENA, SLOBODNA, DIFERENCIRANA -> mpcStara;
        };
    }

    private BigDecimal dohvatiPdvStopu(ArtikalPoslovnica ap) {
        if (ap.getArtikalKompanija() != null && ap.getArtikalKompanija().getPdv() != null) {
            BigDecimal pdvPostotak = ap.getArtikalKompanija().getPdv();
            return pdvPostotak.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private NivelacijaStavka buildStavka(Long idArtikla, BigDecimal kolicina,
                                         BigDecimal vpcStara, BigDecimal vpcNova,
                                         BigDecimal mpcStara, BigDecimal mpcNova,
                                         BigDecimal iznosNivelacije) {
        NivelacijaStavka stavka = new NivelacijaStavka();
        stavka.setIdArtikla(idArtikla);
        stavka.setKolicina(kolicina);
        stavka.setVpcStara(vpcStara);
        stavka.setVpcNova(vpcNova);
        stavka.setMpcStara(mpcStara);
        stavka.setMpcNova(mpcNova);
        stavka.setIznosNivelacije(iznosNivelacije);
        return stavka;
    }

    private Nivelacija buildNivelacija(String broj, LocalDate datum, VrstaNivelacije vrsta,
                                       Long idKompanije, Long idPoslovnice, String napomena) {
        Nivelacija nivelacija = new Nivelacija();
        nivelacija.setBroj(broj);
        nivelacija.setDatum(datum);
        nivelacija.setVrsta(vrsta);
        nivelacija.setIdKompanije(idKompanije);
        nivelacija.setIdPoslovnice(idPoslovnice);
        nivelacija.setNapomena(napomena);
        return nivelacija;
    }
}
