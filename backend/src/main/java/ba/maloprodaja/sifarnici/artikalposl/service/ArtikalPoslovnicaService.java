package ba.maloprodaja.sifarnici.artikalposl.service;

import ba.maloprodaja.common.exception.BusinessException;
import ba.maloprodaja.common.exception.ResourceNotFoundException;
import ba.maloprodaja.sifarnici.artikalkomp.entity.ArtikalKompanija;
import ba.maloprodaja.sifarnici.artikalkomp.entity.TipMarze;
import ba.maloprodaja.sifarnici.artikalkomp.repository.ArtikalKompanijeRepository;
import ba.maloprodaja.sifarnici.artikalposl.dto.ArtikalPoslovnicaDTO;
import ba.maloprodaja.sifarnici.artikalposl.entity.ArtikalPoslovnica;
import ba.maloprodaja.sifarnici.artikalposl.repository.ArtikalPoslovnicaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtikalPoslovnicaService implements IArtikalPoslovnicaService {

    private final ArtikalPoslovnicaRepository artikalPoslovnicaRepository;
    private final ArtikalKompanijeRepository artikalKompanijeRepository;

    @Override
    public List<ArtikalPoslovnicaDTO.ListItemDTO> listByPoslovnica(Long idPoslovnice) {
        Map<Long, BigDecimal> kolicineMap = artikalPoslovnicaRepository.ukupnaKolicinaMapByPoslovnica(idPoslovnice);
        return artikalPoslovnicaRepository.findByIdPoslovnice(idPoslovnice)
                .stream()
                .map(ap -> toDTO(ap, kolicineMap.getOrDefault(ap.getId(), BigDecimal.ZERO)))
                .toList();
    }

    @Override
    public List<ArtikalPoslovnicaDTO.ListItemDTO> listByArtikalKompanija(Long idArtikla) {
        Map<Long, BigDecimal> kolicineMap = artikalPoslovnicaRepository.ukupnaKolicinaMapByArtikla(idArtikla);
        return artikalPoslovnicaRepository.findByIdArtikla(idArtikla)
                .stream()
                .map(ap -> toDTO(ap, kolicineMap.getOrDefault(ap.getId(), BigDecimal.ZERO)))
                .toList();
    }

    @Override
    @Transactional
    public ArtikalPoslovnicaDTO.ListItemDTO create(ArtikalPoslovnicaDTO.CreateDTO dto, Long idKompanije) {
        ArtikalKompanija artikalKompanija = artikalKompanijeRepository.findById(dto.idArtikla())
                .orElseThrow(() -> new ResourceNotFoundException("ArtikalKompanija", dto.idArtikla()));

        if (artikalPoslovnicaRepository.existsByIdArtiklaAndIdPoslovnice(dto.idArtikla(), dto.idPoslovnice())) {
            throw new BusinessException("Artikal je već dodan u ovu poslovnicu.");
        }

        TipMarze tipMarze = dto.tipMarze() != null ? dto.tipMarze() : TipMarze.SLOBODNA;

        ArtikalPoslovnica ap = new ArtikalPoslovnica();
        ap.setIdArtikla(dto.idArtikla());
        ap.setIdPoslovnice(dto.idPoslovnice());
        ap.setIdKompanije(idKompanije);
        ap.setVpc(dto.vpc());
        ap.setMarza(dto.marza());
        ap.setTipMarze(tipMarze);
        ap.setMpc(kalkulisajMpc(dto.vpc(), dto.marza(), artikalKompanija.getPdv()));
        ap.setAktivan(true);

        ArtikalPoslovnica saved = artikalPoslovnicaRepository.save(ap);
        log.info("Kreiran ArtikalPoslovnica: idArtikla={}, idPoslovnice={}", dto.idArtikla(), dto.idPoslovnice());
        return toDTO(saved, BigDecimal.ZERO);
    }

    @Override
    @Transactional
    public ArtikalPoslovnicaDTO.ListItemDTO update(Long id, ArtikalPoslovnicaDTO.UpdateDTO dto) {
        ArtikalPoslovnica ap = artikalPoslovnicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ArtikalPoslovnica", id));

        ArtikalKompanija artikalKompanija = artikalKompanijeRepository.findById(ap.getIdArtikla())
                .orElseThrow(() -> new ResourceNotFoundException("ArtikalKompanija", ap.getIdArtikla()));

        if (dto.vpc() != null) {
            ap.setVpc(dto.vpc());
        }
        if (dto.marza() != null) {
            ap.setMarza(dto.marza());
        }
        if (dto.tipMarze() != null) {
            ap.setTipMarze(dto.tipMarze());
        }
        if (dto.aktivan() != null) {
            ap.setAktivan(dto.aktivan());
        }
        if (dto.popustProcenat() != null) {
            ap.setPopustProcenat(dto.popustProcenat());
        }

        // Recalculate MPC from current vpc/marza after applying updates
        ap.setMpc(kalkulisajMpc(ap.getVpc(), ap.getMarza(), artikalKompanija.getPdv()));

        ArtikalPoslovnica saved = artikalPoslovnicaRepository.save(ap);
        log.info("Ažuriran ArtikalPoslovnica: id={}", id);
        return toDTO(saved, BigDecimal.ZERO);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        ArtikalPoslovnica ap = artikalPoslovnicaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ArtikalPoslovnica", id));
        ap.setAktivan(false);
        artikalPoslovnicaRepository.save(ap);
        log.info("Deaktiviran ArtikalPoslovnica: id={}", id);
    }

    @Override
    public List<ArtikalPoslovnicaDTO.ListItemDTO> listByKompanija(Long idKompanije) {
        return artikalPoslovnicaRepository.findByIdKompanije(idKompanije)
                .stream()
                .map(ap -> toDTO(ap, BigDecimal.ZERO))
                .toList();
    }

    @Override
    @Transactional
    public void batchUpdateMpc(List<ArtikalPoslovnicaDTO.BatchMpcUpdateDTO> updates) {
        for (ArtikalPoslovnicaDTO.BatchMpcUpdateDTO dto : updates) {
            ArtikalPoslovnica ap = artikalPoslovnicaRepository.findById(dto.id())
                    .orElseThrow(() -> new ResourceNotFoundException("ArtikalPoslovnica", dto.id()));
            ap.setMpc(dto.novaMpc());
            ap.setTipMarze(TipMarze.FIKSNA_CIJENA);
            artikalPoslovnicaRepository.save(ap);
            log.info("Batch MPC update: id={}, novaMpc={}", dto.id(), dto.novaMpc());
        }
    }

    @Override
    @Transactional
    public void batchUpdatePopust(List<ArtikalPoslovnicaDTO.BatchPopustUpdateDTO> updates) {
        for (ArtikalPoslovnicaDTO.BatchPopustUpdateDTO dto : updates) {
            ArtikalPoslovnica ap = artikalPoslovnicaRepository.findById(dto.id())
                    .orElseThrow(() -> new ResourceNotFoundException("ArtikalPoslovnica", dto.id()));
            ap.setPopustProcenat(dto.popustProcenat());
            artikalPoslovnicaRepository.save(ap);
            log.info("Batch popust update: id={}, popustProcenat={}", dto.id(), dto.popustProcenat());
        }
    }

    // ---- Private helpers ----

    private BigDecimal kalkulisajMpc(BigDecimal vpc, BigDecimal marza, BigDecimal pdv) {
        if (vpc == null) {
            return null;
        }
        BigDecimal marzaProcenat = marza != null ? marza : BigDecimal.ZERO;
        BigDecimal pdvProcenat = pdv != null ? pdv : BigDecimal.ZERO;

        BigDecimal marza100 = marzaProcenat.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        BigDecimal pdv100 = pdvProcenat.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        BigDecimal cijenaSaMarza = vpc.add(vpc.multiply(marza100));
        BigDecimal cijenaSaPdv = cijenaSaMarza.add(vpc.multiply(pdv100));

        return cijenaSaPdv.setScale(4, RoundingMode.HALF_UP);
    }

    private ArtikalPoslovnicaDTO.ListItemDTO toDTO(ArtikalPoslovnica ap, BigDecimal ukupnaKolicina) {
        String artikalNaziv = null;
        String artikalSifra = null;

        if (ap.getArtikalKompanija() != null) {
            artikalNaziv = ap.getArtikalKompanija().getNaziv();
            artikalSifra = ap.getArtikalKompanija().getSifra();
        } else {
            ArtikalKompanija ak = artikalKompanijeRepository.findById(ap.getIdArtikla()).orElse(null);
            if (ak != null) {
                artikalNaziv = ak.getNaziv();
                artikalSifra = ak.getSifra();
            }
        }

        return new ArtikalPoslovnicaDTO.ListItemDTO(
                ap.getId(),
                ap.getIdArtikla(),
                artikalNaziv,
                artikalSifra,
                ap.getIdPoslovnice(),
                ap.getVpc(),
                ap.getMarza(),
                ap.getTipMarze(),
                ap.getMpc(),
                ap.isAktivan(),
                ukupnaKolicina != null ? ukupnaKolicina : BigDecimal.ZERO,
                ap.getPopustProcenat()
        );
    }
}
