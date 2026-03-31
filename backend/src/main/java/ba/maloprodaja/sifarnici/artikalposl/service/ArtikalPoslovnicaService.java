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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtikalPoslovnicaService implements IArtikalPoslovnicaService {

    private final ArtikalPoslovnicaRepository artikalPoslovnicaRepository;
    private final ArtikalKompanijeRepository artikalKompanijeRepository;

    @Override
    public List<ArtikalPoslovnicaDTO.ListItemDTO> listByPoslovnica(Long idPoslovnice) {
        return artikalPoslovnicaRepository.findByIdPoslovnice(idPoslovnice)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public List<ArtikalPoslovnicaDTO.ListItemDTO> listByArtikalKompanija(Long idArtikla) {
        return artikalPoslovnicaRepository.findByIdArtikla(idArtikla)
                .stream()
                .map(this::toDTO)
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
        ap.setKolicina(dto.kolicina() != null ? dto.kolicina() : BigDecimal.ZERO);
        ap.setMinZaliha(dto.minZaliha());
        ap.setOptimalnaZaliha(dto.optimalnaZaliha());
        ap.setAktivan(true);

        ArtikalPoslovnica saved = artikalPoslovnicaRepository.save(ap);
        log.info("Kreiran ArtikalPoslovnica: idArtikla={}, idPoslovnice={}", dto.idArtikla(), dto.idPoslovnice());
        return toDTO(saved);
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
        if (dto.kolicina() != null) {
            ap.setKolicina(dto.kolicina());
        }
        if (dto.minZaliha() != null) {
            ap.setMinZaliha(dto.minZaliha());
        }
        if (dto.optimalnaZaliha() != null) {
            ap.setOptimalnaZaliha(dto.optimalnaZaliha());
        }
        if (dto.aktivan() != null) {
            ap.setAktivan(dto.aktivan());
        }

        // Recalculate MPC from current vpc/marza after applying updates
        ap.setMpc(kalkulisajMpc(ap.getVpc(), ap.getMarza(), artikalKompanija.getPdv()));

        ArtikalPoslovnica saved = artikalPoslovnicaRepository.save(ap);
        log.info("Ažuriran ArtikalPoslovnica: id={}", id);
        return toDTO(saved);
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

    private ArtikalPoslovnicaDTO.ListItemDTO toDTO(ArtikalPoslovnica ap) {
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
                ap.getKolicina(),
                ap.getMinZaliha(),
                ap.getOptimalnaZaliha(),
                ap.isAktivan()
        );
    }
}
