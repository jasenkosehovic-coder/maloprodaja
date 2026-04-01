package ba.maloprodaja.dokumenti.faktura.service;

import ba.maloprodaja.common.exception.BusinessException;
import ba.maloprodaja.common.exception.ResourceNotFoundException;
import ba.maloprodaja.dokumenti.enums.StatusFakture;
import ba.maloprodaja.dokumenti.faktura.dto.UlaznaFakturaDTO;
import ba.maloprodaja.dokumenti.faktura.entity.UlaznaFaktura;
import ba.maloprodaja.dokumenti.faktura.entity.UlaznaFakturaStavka;
import ba.maloprodaja.dokumenti.faktura.repository.UlaznaFakturaRepository;
import ba.maloprodaja.dokumenti.faktura.repository.UlaznaFakturaStavkaRepository;
import ba.maloprodaja.dokumenti.nivelacija.service.INivelacijaService;
import ba.maloprodaja.sifarnici.artikalkomp.entity.ArtikalKompanija;
import ba.maloprodaja.sifarnici.artikalkomp.repository.ArtikalKompanijeRepository;
import ba.maloprodaja.sifarnici.artikalposl.entity.ArtikalPoslovnica;
import ba.maloprodaja.sifarnici.artikalposl.repository.ArtikalPoslovnicaRepository;
import ba.maloprodaja.sifarnici.dobavljac.entity.Dobavljac;
import ba.maloprodaja.sifarnici.dobavljac.repository.DobavljacRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UlaznaFakturaService implements IUlaznaFakturaService {

    private final UlaznaFakturaRepository fakturaRepository;
    private final UlaznaFakturaStavkaRepository stavkaRepository;
    private final ArtikalPoslovnicaRepository artikalPoslovnicaRepository;
    private final DobavljacRepository dobavljacRepository;
    private final ArtikalKompanijeRepository artikalKompRepository;
    private final INivelacijaService nivelacijaService;

    @Override
    public List<UlaznaFakturaDTO.ListItemDTO> listAll(Long idKompanije, Long idPoslovnice) {
        List<UlaznaFaktura> fakture = fakturaRepository.findByIdKompanijeAndIdPoslovnice(idKompanije, idPoslovnice);

        List<Long> dobavljacIds = fakture.stream()
                .map(UlaznaFaktura::getIdDobavljaca)
                .distinct()
                .toList();

        Map<Long, String> naziviDobavljaca = dobavljacRepository.findAllById(dobavljacIds)
                .stream()
                .collect(Collectors.toMap(Dobavljac::getId, Dobavljac::getNaziv));

        return fakture.stream()
                .map(f -> new UlaznaFakturaDTO.ListItemDTO(
                        f.getId(),
                        f.getBroj(),
                        f.getDatum(),
                        f.getDatumValute(),
                        f.getStatus().name(),
                        naziviDobavljaca.getOrDefault(f.getIdDobavljaca(), ""),
                        f.getUkupnoBezPdv(),
                        f.getUkupnoPdv(),
                        f.getUkupno()
                ))
                .toList();
    }

    @Override
    public UlaznaFakturaDTO.DetailDTO findById(Long id) {
        UlaznaFaktura faktura = fakturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UlaznaFaktura", id));

        List<UlaznaFakturaStavka> stavke = stavkaRepository.findByFakturaId(id);

        List<Long> artikalIds = stavke.stream()
                .map(UlaznaFakturaStavka::getIdArtikla)
                .distinct()
                .toList();

        Map<Long, ArtikalKompanija> artikliMap = artikalKompRepository.findAllById(artikalIds)
                .stream()
                .collect(Collectors.toMap(ArtikalKompanija::getId, a -> a));

        Map<Long, String> artikalNazivi = artikliMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getNaziv()));

        Map<Long, String> artikalSifre = artikliMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getSifra()));

        String nazivDobavljaca = dobavljacRepository.findById(faktura.getIdDobavljaca())
                .map(Dobavljac::getNaziv)
                .orElse("");

        return toDetailDTO(faktura, stavke, artikalNazivi, artikalSifre, nazivDobavljaca);
    }

    @Override
    @Transactional
    public UlaznaFakturaDTO.DetailDTO create(UlaznaFakturaDTO.CreateDTO dto, Long idKompanije, Long idPoslovnice) {
        if (!dobavljacRepository.existsById(dto.idDobavljaca())) {
            throw new BusinessException("Dobavljač sa ID=" + dto.idDobavljaca() + " ne postoji.");
        }

        if (fakturaRepository.existsByIdKompanijeAndIdPoslovniceAndBroj(idKompanije, idPoslovnice, dto.broj())) {
            throw new BusinessException("Faktura sa brojem '" + dto.broj() + "' već postoji za ovu poslovnicu.");
        }

        UlaznaFaktura faktura = new UlaznaFaktura();
        faktura.setIdKompanije(idKompanije);
        faktura.setIdPoslovnice(idPoslovnice);
        faktura.setIdDobavljaca(dto.idDobavljaca());
        faktura.setBroj(dto.broj());
        faktura.setDatum(dto.datum());
        faktura.setDatumValute(dto.datumValute());
        faktura.setNapomena(dto.napomena());
        faktura.setStatus(StatusFakture.NACRT);

        if (dto.ukupnoBezPdv() != null) {
            faktura.setUkupnoBezPdv(dto.ukupnoBezPdv());
        }
        if (dto.ukupno() != null) {
            faktura.setUkupno(dto.ukupno());
            if (dto.ukupnoBezPdv() != null) {
                faktura.setUkupnoPdv(dto.ukupno().subtract(dto.ukupnoBezPdv()));
            } else {
                faktura.setUkupnoPdv(BigDecimal.ZERO);
            }
        }

        UlaznaFaktura savedFaktura = fakturaRepository.save(faktura);

        log.info("Kreirana ulazna faktura: broj='{}', idKompanije={}, idPoslovnice={}", dto.broj(), idKompanije, idPoslovnice);

        return findById(savedFaktura.getId());
    }

    @Override
    @Transactional
    public UlaznaFakturaDTO.DetailDTO update(Long id, UlaznaFakturaDTO.UpdateDTO dto) {
        UlaznaFaktura faktura = fakturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UlaznaFaktura", id));

        if (faktura.getStatus() != StatusFakture.NACRT) {
            throw new BusinessException("Faktura nije u statusu NACRT.");
        }

        if (dto.idDobavljaca() != null) {
            if (!dobavljacRepository.existsById(dto.idDobavljaca())) {
                throw new BusinessException("Dobavljač sa ID=" + dto.idDobavljaca() + " ne postoji.");
            }
            faktura.setIdDobavljaca(dto.idDobavljaca());
        }

        if (dto.broj() != null) {
            faktura.setBroj(dto.broj());
        }
        if (dto.datum() != null) {
            faktura.setDatum(dto.datum());
        }
        if (dto.datumValute() != null) {
            faktura.setDatumValute(dto.datumValute());
        }
        if (dto.napomena() != null) {
            faktura.setNapomena(dto.napomena());
        }

        fakturaRepository.save(faktura);

        log.info("Ažurirana ulazna faktura: id={}", id);

        return findById(id);
    }

    @Override
    @Transactional
    public void potvrdi(Long id) {
        UlaznaFaktura faktura = fakturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UlaznaFaktura", id));

        if (faktura.getStatus() != StatusFakture.NACRT) {
            throw new BusinessException("Faktura nije u statusu NACRT.");
        }

        List<UlaznaFakturaStavka> stavke = stavkaRepository.findByFakturaId(id);

        if (stavke.isEmpty()) {
            throw new BusinessException("Faktura nema stavki i ne može biti potvrđena.");
        }

        for (UlaznaFakturaStavka stavka : stavke) {
            ArtikalPoslovnica artikalPoslovnica = artikalPoslovnicaRepository
                    .findByIdArtiklaAndIdPoslovnice(stavka.getIdArtikla(), faktura.getIdPoslovnice())
                    .orElseThrow(() -> new BusinessException(
                            "Artikal sa ID=" + stavka.getIdArtikla() + " nije pronađen na poslovnici."));
            artikalPoslovnica.setKolicina(artikalPoslovnica.getKolicina().add(stavka.getKolicina()));
            artikalPoslovnicaRepository.save(artikalPoslovnica);
        }

        faktura.setStatus(StatusFakture.POTVRDJENO);
        fakturaRepository.save(faktura);

        log.info("Potvrđena ulazna faktura: id={}", id);

        nivelacijaService.kreirajAutomatskuZaFakturu(id, faktura.getIdKompanije(), faktura.getIdPoslovnice());
    }

    @Override
    @Transactional
    public void storno(Long id) {
        UlaznaFaktura faktura = fakturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UlaznaFaktura", id));

        if (faktura.getStatus() != StatusFakture.POTVRDJENO) {
            throw new BusinessException("Samo potvrđena faktura može biti stornirana.");
        }

        List<UlaznaFakturaStavka> stavke = stavkaRepository.findByFakturaId(id);

        for (UlaznaFakturaStavka stavka : stavke) {
            ArtikalPoslovnica artikalPoslovnica = artikalPoslovnicaRepository
                    .findByIdArtiklaAndIdPoslovnice(stavka.getIdArtikla(), faktura.getIdPoslovnice())
                    .orElseThrow(() -> new BusinessException(
                            "Artikal sa ID=" + stavka.getIdArtikla() + " nije pronađen na poslovnici."));

            BigDecimal novaKolicina = artikalPoslovnica.getKolicina().subtract(stavka.getKolicina());
            if (novaKolicina.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(
                        "Storno nije moguć — artikal sa ID=" + stavka.getIdArtikla()
                                + " ima nedovoljnu količinu na stanju.");
            }
            artikalPoslovnica.setKolicina(novaKolicina);
            artikalPoslovnicaRepository.save(artikalPoslovnica);
        }

        faktura.setStatus(StatusFakture.STORNIRANO);
        fakturaRepository.save(faktura);

        log.info("Stornirana ulazna faktura: id={}", id);
    }

    @Override
    @Transactional
    public UlaznaFakturaDTO.DetailDTO addStavka(Long fakturaId, UlaznaFakturaDTO.AddStavkaDTO dto) {
        UlaznaFaktura faktura = fakturaRepository.findById(fakturaId)
                .orElseThrow(() -> new ResourceNotFoundException("UlaznaFaktura", fakturaId));

        if (faktura.getStatus() != StatusFakture.NACRT) {
            throw new BusinessException("Stavke je moguće dodavati samo na fakture u statusu NACRT.");
        }

        BigDecimal iznosPdv = izracunajIznosPdv(dto.vpc(), dto.kolicina(), dto.pdvStopa());
        BigDecimal ukupnoStavka = dto.vpc().multiply(dto.kolicina()).add(iznosPdv).setScale(4, RoundingMode.HALF_UP);

        UlaznaFakturaStavka stavka = new UlaznaFakturaStavka();
        stavka.setFaktura(faktura);
        stavka.setIdArtikla(dto.idArtikla());
        stavka.setKolicina(dto.kolicina());
        stavka.setVpc(dto.vpc());
        stavka.setPdvStopa(dto.pdvStopa());
        stavka.setIznosPdv(iznosPdv);
        stavka.setUkupno(ukupnoStavka);
        stavkaRepository.save(stavka);

        List<UlaznaFakturaStavka> sveStavke = stavkaRepository.findByFakturaId(fakturaId);
        azurirajZbrojeve(faktura, sveStavke);
        fakturaRepository.save(faktura);

        log.info("Dodana stavka na fakturu id={}, artikal id={}", fakturaId, dto.idArtikla());

        return findById(fakturaId);
    }

    @Override
    @Transactional
    public void removeStavka(Long fakturaId, Long stavkaId) {
        UlaznaFaktura faktura = fakturaRepository.findById(fakturaId)
                .orElseThrow(() -> new ResourceNotFoundException("UlaznaFaktura", fakturaId));

        if (faktura.getStatus() != StatusFakture.NACRT) {
            throw new BusinessException("Stavke je moguće uklanjati samo sa faktura u statusu NACRT.");
        }

        UlaznaFakturaStavka stavka = stavkaRepository.findById(stavkaId)
                .orElseThrow(() -> new ResourceNotFoundException("UlaznaFakturaStavka", stavkaId));

        if (!stavka.getFaktura().getId().equals(fakturaId)) {
            throw new BusinessException("Stavka sa ID=" + stavkaId + " ne pripada fakturi sa ID=" + fakturaId + ".");
        }

        stavkaRepository.delete(stavka);

        List<UlaznaFakturaStavka> preostaleStavke = stavkaRepository.findByFakturaId(fakturaId);
        azurirajZbrojeve(faktura, preostaleStavke);
        fakturaRepository.save(faktura);

        log.info("Uklonjena stavka id={} sa fakture id={}", stavkaId, fakturaId);
    }

    private List<UlaznaFakturaStavka> kreirajStavke(
            List<UlaznaFakturaDTO.CreateStavkaDTO> stavkeDtos,
            UlaznaFaktura faktura) {
        return stavkeDtos.stream()
                .map(s -> {
                    BigDecimal iznosPdv = izracunajIznosPdv(s.vpc(), s.kolicina(), s.pdvStopa());
                    BigDecimal ukupno = s.vpc().multiply(s.kolicina()).add(iznosPdv).setScale(4, RoundingMode.HALF_UP);

                    UlaznaFakturaStavka stavka = new UlaznaFakturaStavka();
                    stavka.setFaktura(faktura);
                    stavka.setIdArtikla(s.idArtikla());
                    stavka.setKolicina(s.kolicina());
                    stavka.setVpc(s.vpc());
                    stavka.setPdvStopa(s.pdvStopa());
                    stavka.setIznosPdv(iznosPdv);
                    stavka.setUkupno(ukupno);
                    return stavka;
                })
                .toList();
    }

    private void azurirajZbrojeve(UlaznaFaktura faktura, List<UlaznaFakturaStavka> stavke) {
        BigDecimal ukupnoBezPdv = stavke.stream()
                .map(s -> s.getVpc().multiply(s.getKolicina()).setScale(4, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ukupnoPdv = stavke.stream()
                .map(UlaznaFakturaStavka::getIznosPdv)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ukupno = stavke.stream()
                .map(UlaznaFakturaStavka::getUkupno)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        faktura.setUkupnoBezPdv(ukupnoBezPdv);
        faktura.setUkupnoPdv(ukupnoPdv);
        faktura.setUkupno(ukupno);
    }

    private BigDecimal izracunajIznosPdv(BigDecimal vpc, BigDecimal kolicina, BigDecimal pdvStopa) {
        return vpc.multiply(kolicina).multiply(pdvStopa).setScale(4, RoundingMode.HALF_UP);
    }

    private UlaznaFakturaDTO.DetailDTO toDetailDTO(
            UlaznaFaktura faktura,
            List<UlaznaFakturaStavka> stavke,
            Map<Long, String> artikalNazivi,
            Map<Long, String> artikalSifre,
            String nazivDobavljaca) {

        List<UlaznaFakturaDTO.StavkaDTO> stavkeDtos = stavke.stream()
                .map(s -> new UlaznaFakturaDTO.StavkaDTO(
                        s.getId(),
                        s.getIdArtikla(),
                        artikalNazivi.getOrDefault(s.getIdArtikla(), ""),
                        artikalSifre.getOrDefault(s.getIdArtikla(), ""),
                        s.getKolicina(),
                        s.getVpc(),
                        s.getPdvStopa(),
                        s.getIznosPdv(),
                        s.getUkupno()
                ))
                .toList();

        return new UlaznaFakturaDTO.DetailDTO(
                faktura.getId(),
                faktura.getBroj(),
                faktura.getDatum(),
                faktura.getDatumValute(),
                faktura.getStatus().name(),
                faktura.getIdDobavljaca(),
                nazivDobavljaca,
                faktura.getUkupnoBezPdv(),
                faktura.getUkupnoPdv(),
                faktura.getUkupno(),
                faktura.getNapomena(),
                stavkeDtos
        );
    }
}
