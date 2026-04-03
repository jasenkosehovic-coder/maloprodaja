package ba.maloprodaja.dokumenti.otpremnica.service;

import ba.maloprodaja.common.exception.BusinessException;
import ba.maloprodaja.common.exception.ResourceNotFoundException;
import ba.maloprodaja.dokumenti.enums.StatusOtpremnice;
import ba.maloprodaja.dokumenti.nivelacija.service.INivelacijaService;
import ba.maloprodaja.dokumenti.nivelacija.service.INivelacijaService.NivelacijaStavkaInfo;
import ba.maloprodaja.dokumenti.otpremnica.dto.OtpremnicaDTO;
import ba.maloprodaja.dokumenti.otpremnica.entity.Otpremnica;
import ba.maloprodaja.dokumenti.otpremnica.entity.OtpremnicaStavka;
import ba.maloprodaja.dokumenti.otpremnica.repository.OtpremnicaRepository;
import ba.maloprodaja.dokumenti.otpremnica.repository.OtpremnicaStavkaRepository;
import ba.maloprodaja.poslovnica.entity.Poslovnica;
import ba.maloprodaja.poslovnica.repository.PoslovnicaRepository;
import ba.maloprodaja.sifarnici.artikalkomp.entity.ArtikalKompanija;
import ba.maloprodaja.sifarnici.artikalkomp.repository.ArtikalKompanijeRepository;
import ba.maloprodaja.sifarnici.artikalposl.entity.ArtikalPoslovnica;
import ba.maloprodaja.sifarnici.artikalposl.repository.ArtikalPoslovnicaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OtpremnicaService implements IOtpremnicaService {

    private static final BigDecimal VPC_TOLERANCE = new BigDecimal("0.0001");

    private final OtpremnicaRepository otpremnicaRepository;
    private final OtpremnicaStavkaRepository stavkaRepository;
    private final ArtikalPoslovnicaRepository artikalPoslovnicaRepository;
    private final ArtikalKompanijeRepository artikalKompRepository;
    private final PoslovnicaRepository poslovnicaRepository;
    private final INivelacijaService nivelacijaService;

    @Override
    public List<OtpremnicaDTO.ListItemDTO> listAll(Long idKompanije, Long idPoslovnice, Integer godina) {
        List<Otpremnica> poslanjeOtpremnice = godina != null
                ? otpremnicaRepository.findByIdKompanijeAndIdPoslovnicePosiljaocaAndGodina(idKompanije, idPoslovnice, godina)
                : otpremnicaRepository.findByIdKompanijeAndIdPoslovnicePosiljaoca(idKompanije, idPoslovnice);

        List<Otpremnica> primljeneOtpremnice = godina != null
                ? otpremnicaRepository.findByIdKompanijeAndIdPoslovnicePrimaoсaAndGodina(idKompanije, idPoslovnice, godina)
                : otpremnicaRepository.findByIdKompanijeAndIdPoslovnicePrimaoca(idKompanije, idPoslovnice);

        List<Otpremnica> svjeOtpremnice = Stream.concat(poslanjeOtpremnice.stream(), primljeneOtpremnice.stream())
                .collect(Collectors.toMap(Otpremnica::getId, o -> o, (a, b) -> a))
                .values()
                .stream()
                .sorted((a, b) -> b.getDatum().compareTo(a.getDatum()))
                .toList();

        List<Long> otpremnicaIds = svjeOtpremnice.stream()
                .map(Otpremnica::getId)
                .toList();

        Map<Long, Long> brojStavkiMap = stavkaRepository.findByOtpremnicaIdIn(otpremnicaIds)
                .stream()
                .collect(Collectors.groupingBy(s -> s.getOtpremnica().getId(), Collectors.counting()));

        List<Long> poslovnicaIds = svjeOtpremnice.stream()
                .flatMap(o -> Stream.of(o.getIdPoslovnicePosiljaoca(), o.getIdPoslovnicePrimaoca()))
                .distinct()
                .toList();

        Map<Long, String> poslovnicaNazivMap = poslovnicaRepository.findAllById(poslovnicaIds)
                .stream()
                .collect(Collectors.toMap(Poslovnica::getId, Poslovnica::getNaziv));

        return svjeOtpremnice.stream()
                .map(o -> new OtpremnicaDTO.ListItemDTO(
                        o.getId(),
                        o.getBroj(),
                        o.getDatum(),
                        o.getStatus().name(),
                        o.getIdPoslovnicePosiljaoca(),
                        poslovnicaNazivMap.getOrDefault(o.getIdPoslovnicePosiljaoca(), ""),
                        o.getIdPoslovnicePrimaoca(),
                        poslovnicaNazivMap.getOrDefault(o.getIdPoslovnicePrimaoca(), ""),
                        brojStavkiMap.getOrDefault(o.getId(), 0L).intValue()
                ))
                .toList();
    }

    @Override
    public OtpremnicaDTO.DetailDTO findById(Long id) {
        Otpremnica otpremnica = otpremnicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Otpremnica", id));

        List<OtpremnicaStavka> stavke = stavkaRepository.findByOtpremnicaId(id);

        List<Long> artikalIds = stavke.stream()
                .map(OtpremnicaStavka::getIdArtikla)
                .distinct()
                .toList();

        Map<Long, ArtikalKompanija> artikliMap = artikalKompRepository.findAllById(artikalIds)
                .stream()
                .collect(Collectors.toMap(ArtikalKompanija::getId, a -> a));

        List<Long> poslovnicaIds = List.of(
                otpremnica.getIdPoslovnicePosiljaoca(),
                otpremnica.getIdPoslovnicePrimaoca()
        );

        Map<Long, String> poslovnicaNazivMap = poslovnicaRepository.findAllById(poslovnicaIds)
                .stream()
                .collect(Collectors.toMap(Poslovnica::getId, Poslovnica::getNaziv));

        List<OtpremnicaDTO.StavkaDTO> stavkeDtos = stavke.stream()
                .map(s -> {
                    ArtikalKompanija ak = artikliMap.get(s.getIdArtikla());
                    return new OtpremnicaDTO.StavkaDTO(
                            s.getId(),
                            s.getIdArtikla(),
                            ak != null ? ak.getNaziv() : "",
                            ak != null ? ak.getSifra() : "",
                            s.getKolicina(),
                            s.getVpcPosiljalac(),
                            s.getMpcPosiljalac()
                    );
                })
                .toList();

        return new OtpremnicaDTO.DetailDTO(
                otpremnica.getId(),
                otpremnica.getBroj(),
                otpremnica.getDatum(),
                otpremnica.getStatus().name(),
                otpremnica.getIdPoslovnicePosiljaoca(),
                poslovnicaNazivMap.getOrDefault(otpremnica.getIdPoslovnicePosiljaoca(), ""),
                otpremnica.getIdPoslovnicePrimaoca(),
                poslovnicaNazivMap.getOrDefault(otpremnica.getIdPoslovnicePrimaoca(), ""),
                otpremnica.getNapomena(),
                stavkeDtos
        );
    }

    @Override
    @Transactional
    public OtpremnicaDTO.DetailDTO create(OtpremnicaDTO.CreateDTO dto, Long idKompanije, Long idPoslovnicePosiljaoca) {
        if (dto.idPoslovnicePrimaoca().equals(idPoslovnicePosiljaoca)) {
            throw new BusinessException("Pošiljalac i primalac ne mogu biti ista poslovnica.");
        }

        if (otpremnicaRepository.existsByIdKompanijeAndBroj(idKompanije, dto.broj())) {
            throw new BusinessException("Otpremnica sa brojem '" + dto.broj() + "' već postoji za ovu kompaniju.");
        }

        Otpremnica otpremnica = new Otpremnica();
        otpremnica.setIdKompanije(idKompanije);
        otpremnica.setIdPoslovnicePosiljaoca(idPoslovnicePosiljaoca);
        otpremnica.setIdPoslovnicePrimaoca(dto.idPoslovnicePrimaoca());
        otpremnica.setBroj(dto.broj());
        otpremnica.setDatum(dto.datum());
        otpremnica.setGodina(dto.datum().getYear());
        otpremnica.setNapomena(dto.napomena());
        otpremnica.setStatus(StatusOtpremnice.KREIRANA);

        Otpremnica savedOtpremnica = otpremnicaRepository.save(otpremnica);

        List<OtpremnicaStavka> stavke = new ArrayList<>();

        for (OtpremnicaDTO.CreateStavkaDTO stavkaDto : dto.stavke()) {
            ArtikalPoslovnica artikalPoslovnica = artikalPoslovnicaRepository
                    .findByIdArtiklaAndIdPoslovnice(stavkaDto.idArtikla(), idPoslovnicePosiljaoca)
                    .orElseThrow(() -> new BusinessException(
                            "Artikal sa ID=" + stavkaDto.idArtikla() + " nije pronađen na pošiljaočevoj poslovnici."));

            String nazivArtikla = dohvatiNazivArtikla(stavkaDto.idArtikla());

            if (artikalPoslovnica.getKolicina().compareTo(stavkaDto.kolicina()) < 0) {
                throw new BusinessException("Nedovoljno zalihe za artikal: " + nazivArtikla);
            }

            artikalPoslovnica.setKolicina(artikalPoslovnica.getKolicina().subtract(stavkaDto.kolicina()));
            artikalPoslovnicaRepository.save(artikalPoslovnica);

            OtpremnicaStavka stavka = new OtpremnicaStavka();
            stavka.setOtpremnica(savedOtpremnica);
            stavka.setIdArtikla(stavkaDto.idArtikla());
            stavka.setKolicina(stavkaDto.kolicina());
            stavka.setVpcPosiljalac(artikalPoslovnica.getVpc() != null ? artikalPoslovnica.getVpc() : BigDecimal.ZERO);
            stavka.setMpcPosiljalac(artikalPoslovnica.getMpc() != null ? artikalPoslovnica.getMpc() : BigDecimal.ZERO);
            stavke.add(stavka);
        }

        stavkaRepository.saveAll(stavke);

        log.info("Kreirana otpremnica broj='{}', idKompanije={}, idPoslovnicePosiljaoca={}, stavki={}.",
                dto.broj(), idKompanije, idPoslovnicePosiljaoca, stavke.size());

        return findById(savedOtpremnica.getId());
    }

    @Override
    @Transactional
    public void posalji(Long id) {
        Otpremnica otpremnica = otpremnicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Otpremnica", id));

        if (otpremnica.getStatus() != StatusOtpremnice.KREIRANA) {
            throw new BusinessException("Otpremnica se može poslati samo u statusu KREIRANA. Trenutni status: "
                    + otpremnica.getStatus().name());
        }

        otpremnica.setStatus(StatusOtpremnice.POSLANA);
        otpremnicaRepository.save(otpremnica);

        log.info("Otpremnica id={}, broj='{}' poslana.", id, otpremnica.getBroj());
    }

    @Override
    @Transactional
    public void potvrdiPrijem(Long id, OtpremnicaDTO.PotvrdiPrijemDTO dto, Long idPoslovnicePrimaoca) {
        Otpremnica otpremnica = otpremnicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Otpremnica", id));

        if (otpremnica.getStatus() != StatusOtpremnice.POSLANA && otpremnica.getStatus() != StatusOtpremnice.KREIRANA) {
            throw new BusinessException("Prijem se može potvrditi samo za otpremnice u statusu POSLANA ili KREIRANA. Trenutni status: "
                    + otpremnica.getStatus().name());
        }

        if (!otpremnica.getIdPoslovnicePrimaoca().equals(idPoslovnicePrimaoca)) {
            throw new BusinessException("Nemate pravo potvrditi prijem ove otpremnice.");
        }

        List<OtpremnicaStavka> stavke = stavkaRepository.findByOtpremnicaId(id);

        List<NivelacijaStavkaInfo> stavkeZaNivelaciju = new ArrayList<>();

        for (OtpremnicaStavka stavka : stavke) {
            ArtikalPoslovnica artikalPrimaoca = dohvatiIliKreirajArtikalPoslovnica(
                    stavka, idPoslovnicePrimaoca, otpremnica.getIdPoslovnicePosiljaoca());

            BigDecimal staraVpc = artikalPrimaoca.getVpc() != null ? artikalPrimaoca.getVpc() : BigDecimal.ZERO;

            artikalPrimaoca.setKolicina(artikalPrimaoca.getKolicina().add(stavka.getKolicina()));
            artikalPoslovnicaRepository.save(artikalPrimaoca);

            BigDecimal razlikaVpc = stavka.getVpcPosiljalac().subtract(staraVpc).abs();
            if (razlikaVpc.compareTo(VPC_TOLERANCE) > 0) {
                stavkeZaNivelaciju.add(new NivelacijaStavkaInfo(
                        stavka.getIdArtikla(),
                        artikalPrimaoca.getKolicina(),
                        stavka.getVpcPosiljalac()
                ));
            }
        }

        otpremnica.setStatus(StatusOtpremnice.PRIMLJENA);
        if (dto != null && dto.napomena() != null && !dto.napomena().isBlank()) {
            otpremnica.setNapomena(dto.napomena());
        }
        otpremnicaRepository.save(otpremnica);

        if (!stavkeZaNivelaciju.isEmpty()) {
            nivelacijaService.kreirajAutomatskuZaOtpremnicu(
                    id,
                    otpremnica.getIdKompanije(),
                    idPoslovnicePrimaoca,
                    stavkeZaNivelaciju
            );
        }

        log.info("Potvrđen prijem otpremnice id={}, broj='{}', idPoslovnicePrimaoca={}.",
                id, otpremnica.getBroj(), idPoslovnicePrimaoca);
    }

    @Override
    @Transactional
    public void storno(Long id, Long idKompanije, Long idPoslovnice) {
        Otpremnica otpremnica = otpremnicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Otpremnica", id));

        if (otpremnica.getStatus() == StatusOtpremnice.STORNIRANA) {
            throw new BusinessException("Otpremnica je već stornirana.");
        }

        List<OtpremnicaStavka> stavke = stavkaRepository.findByOtpremnicaId(id);

        if (otpremnica.getStatus() == StatusOtpremnice.KREIRANA || otpremnica.getStatus() == StatusOtpremnice.POSLANA) {
            vratiZalihuPosiljaocу(stavke, otpremnica.getIdPoslovnicePosiljaoca());
        } else if (otpremnica.getStatus() == StatusOtpremnice.PRIMLJENA) {
            oduzmiOdPrimaoca(stavke, otpremnica.getIdPoslovnicePrimaoca());
            vratiZalihuPosiljaocу(stavke, otpremnica.getIdPoslovnicePosiljaoca());
        }

        otpremnica.setStatus(StatusOtpremnice.STORNIRANA);
        otpremnicaRepository.save(otpremnica);

        log.info("Stornirana otpremnica id={}, broj='{}', idPoslovnice={}.",
                id, otpremnica.getBroj(), idPoslovnice);
    }

    private ArtikalPoslovnica dohvatiIliKreirajArtikalPoslovnica(OtpremnicaStavka stavka,
                                                                  Long idPoslovnicePrimaoca,
                                                                  Long idPoslovnicePosiljaoca) {
        return artikalPoslovnicaRepository
                .findByIdArtiklaAndIdPoslovnice(stavka.getIdArtikla(), idPoslovnicePrimaoca)
                .orElseGet(() -> {
                    ArtikalPoslovnica noviAp = kreirajNovuArtikalPoslovnicu(
                            stavka, idPoslovnicePrimaoca, idPoslovnicePosiljaoca);
                    return artikalPoslovnicaRepository.save(noviAp);
                });
    }

    private ArtikalPoslovnica kreirajNovuArtikalPoslovnicu(OtpremnicaStavka stavka,
                                                            Long idPoslovnicePrimaoca,
                                                            Long idPoslovnicePosiljaoca) {
        ArtikalPoslovnica noviAp = new ArtikalPoslovnica();
        noviAp.setIdArtikla(stavka.getIdArtikla());
        noviAp.setIdPoslovnice(idPoslovnicePrimaoca);
        noviAp.setKolicina(BigDecimal.ZERO);
        noviAp.setVpc(stavka.getVpcPosiljalac());
        noviAp.setMpc(stavka.getMpcPosiljalac());

        artikalPoslovnicaRepository
                .findByIdArtiklaAndIdPoslovnice(stavka.getIdArtikla(), idPoslovnicePosiljaoca)
                .ifPresent(posiljalac -> {
                    noviAp.setMarza(posiljalac.getMarza());
                    noviAp.setTipMarze(posiljalac.getTipMarze());
                    noviAp.setIdKompanije(posiljalac.getIdKompanije());
                });

        return noviAp;
    }

    private void vratiZalihuPosiljaocу(List<OtpremnicaStavka> stavke, Long idPoslovnicePosiljaoca) {
        for (OtpremnicaStavka stavka : stavke) {
            artikalPoslovnicaRepository
                    .findByIdArtiklaAndIdPoslovnice(stavka.getIdArtikla(), idPoslovnicePosiljaoca)
                    .ifPresent(ap -> {
                        ap.setKolicina(ap.getKolicina().add(stavka.getKolicina()));
                        artikalPoslovnicaRepository.save(ap);
                    });
        }
    }

    private void oduzmiOdPrimaoca(List<OtpremnicaStavka> stavke, Long idPoslovnicePrimaoca) {
        for (OtpremnicaStavka stavka : stavke) {
            artikalPoslovnicaRepository
                    .findByIdArtiklaAndIdPoslovnice(stavka.getIdArtikla(), idPoslovnicePrimaoca)
                    .ifPresent(ap -> {
                        BigDecimal novaKolicina = ap.getKolicina().subtract(stavka.getKolicina());
                        if (novaKolicina.compareTo(BigDecimal.ZERO) < 0) {
                            throw new BusinessException(
                                    "Storno nije moguć: količina artikla ID=" + stavka.getIdArtikla()
                                    + " kod primaoca bi bila negativna.");
                        }
                        ap.setKolicina(novaKolicina);
                        artikalPoslovnicaRepository.save(ap);
                    });
        }
    }

    private String dohvatiNazivArtikla(Long idArtikla) {
        return artikalKompRepository.findById(idArtikla)
                .map(ArtikalKompanija::getNaziv)
                .orElse("ID=" + idArtikla);
    }
}
