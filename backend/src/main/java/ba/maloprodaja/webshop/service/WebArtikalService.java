package ba.maloprodaja.webshop.service;

import ba.maloprodaja.common.exception.ResourceNotFoundException;
import ba.maloprodaja.sifarnici.artikalkomp.entity.ArtikalKompanija;
import ba.maloprodaja.sifarnici.artikalkomp.repository.ArtikalKompanijeRepository;
import ba.maloprodaja.webshop.dto.WebArtikalDTO;
import ba.maloprodaja.webshop.entity.WebArtikal;
import ba.maloprodaja.webshop.repository.WebArtikalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WebArtikalService implements IWebArtikalService {

    private final WebArtikalRepository webArtikalRepository;
    private final ArtikalKompanijeRepository artikalKompanijeRepository;

    @Override
    public List<WebArtikalDTO.ListItemDTO> list(Long idKompanije) {
        return webArtikalRepository.findByIdKompanijeOrderByIdDesc(idKompanije)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public WebArtikalDTO.ListItemDTO upsert(WebArtikalDTO.CreateDTO dto, Long idKompanije) {
        ArtikalKompanija artikal = artikalKompanijeRepository.findById(dto.idArtikla())
                .orElseThrow(() -> new ResourceNotFoundException("ArtikalKompanija", dto.idArtikla()));

        WebArtikal webArtikal = webArtikalRepository
                .findByIdArtiklaAndIdKompanije(dto.idArtikla(), idKompanije)
                .orElseGet(WebArtikal::new);

        boolean isNew = webArtikal.getId() == null;

        webArtikal.setIdArtikla(dto.idArtikla());
        webArtikal.setIdKompanije(idKompanije);
        webArtikal.setWebNaziv(dto.webNaziv());
        webArtikal.setWebOpis(dto.webOpis());
        webArtikal.setAktivan(dto.aktivan() != null ? dto.aktivan() : true);
        webArtikal.setMpc(dto.mpc());
        webArtikal.setPopust(dto.popust());
        webArtikal.setNovaMpc(dto.novaMpc());
        webArtikal.setMetaTitle(dto.metaTitle());
        webArtikal.setMetaOpis(dto.metaOpis());

        WebArtikal saved = webArtikalRepository.save(webArtikal);

        if (isNew) {
            log.info("Kreiran novi web artikal: idArtikla={}, idKompanije={}", dto.idArtikla(), idKompanije);
        } else {
            log.info("Ažuriran web artikal: id={}, idArtikla={}, idKompanije={}", saved.getId(), dto.idArtikla(), idKompanije);
        }

        saved.setArtikal(artikal);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public WebArtikalDTO.ListItemDTO update(Long id, WebArtikalDTO.UpdateDTO dto) {
        WebArtikal webArtikal = webArtikalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WebArtikal", id));

        webArtikal.setWebNaziv(dto.webNaziv());
        webArtikal.setWebOpis(dto.webOpis());
        if (dto.aktivan() != null) {
            webArtikal.setAktivan(dto.aktivan());
        }
        webArtikal.setMpc(dto.mpc());
        webArtikal.setPopust(dto.popust());
        webArtikal.setNovaMpc(dto.novaMpc());
        webArtikal.setMetaTitle(dto.metaTitle());
        webArtikal.setMetaOpis(dto.metaOpis());

        WebArtikal saved = webArtikalRepository.save(webArtikal);

        // Eagerly resolve the artikal association so toDTO can map naziv/sifra
        ArtikalKompanija artikal = artikalKompanijeRepository.findById(saved.getIdArtikla())
                .orElse(null);
        saved.setArtikal(artikal);

        log.info("Ažuriran web artikal: id={}", id);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        WebArtikal webArtikal = webArtikalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WebArtikal", id));

        webArtikal.setAktivan(false);
        webArtikalRepository.save(webArtikal);
        log.info("Deaktiviran web artikal: id={}", id);
    }

    // ---- Private helpers ----

    private WebArtikalDTO.ListItemDTO toDTO(WebArtikal w) {
        String naziv = null;
        String sifra = null;
        if (w.getArtikal() != null) {
            naziv = w.getArtikal().getNaziv();
            sifra = w.getArtikal().getSifra();
        }
        return new WebArtikalDTO.ListItemDTO(
                w.getId(),
                w.getIdArtikla(),
                naziv,
                sifra,
                w.getWebNaziv(),
                w.getWebOpis(),
                w.getAktivan(),
                w.getMpc(),
                w.getPopust(),
                w.getNovaMpc(),
                w.getMetaTitle(),
                w.getMetaOpis()
        );
    }
}
