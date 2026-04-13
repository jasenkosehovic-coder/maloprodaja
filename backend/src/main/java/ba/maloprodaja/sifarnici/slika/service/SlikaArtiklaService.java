package ba.maloprodaja.sifarnici.slika.service;

import ba.maloprodaja.common.exception.BusinessException;
import ba.maloprodaja.common.exception.ResourceNotFoundException;
import ba.maloprodaja.sifarnici.artikalkomp.repository.ArtikalKompanijeRepository;
import ba.maloprodaja.sifarnici.slika.dto.SlikaArtiklaDTO;
import ba.maloprodaja.sifarnici.slika.entity.SlikaArtikla;
import ba.maloprodaja.sifarnici.slika.repository.SlikaArtiklaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SlikaArtiklaService implements ISlikaArtiklaService {

    private static final Set<String> DOZVOLJENI_TIPOVI = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> DOZVOLJENE_EKSTENZIJE = Set.of("jpg", "jpeg", "png", "webp");

    private final SlikaArtiklaRepository slikaArtiklaRepository;
    private final ArtikalKompanijeRepository artikalKompanijeRepository;

    @Value("${app.upload.slike-putanja}")
    private String uploadPutanja;

    @Value("${app.upload.max-velicina-mb}")
    private long maxVelicinaeMb;

    @Override
    public List<SlikaArtiklaDTO.ListItemDTO> listSlike(Long idArtikla) {
        artikalKompanijeRepository.findById(idArtikla)
                .orElseThrow(() -> new ResourceNotFoundException("ArtikalKompanija", idArtikla));

        return slikaArtiklaRepository.findByIdArtiklaOrderByRedosljed(idArtikla)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public SlikaArtiklaDTO.ListItemDTO uploadSlika(Long idArtikla, MultipartFile file, Long idKompanije) {
        artikalKompanijeRepository.findById(idArtikla)
                .orElseThrow(() -> new ResourceNotFoundException("ArtikalKompanija", idArtikla));

        validateFile(file);

        String relativnaPutanja = spremiNaDisk(file, idKompanije, idArtikla);

        int redosljed = slikaArtiklaRepository.findByIdArtiklaOrderByRedosljed(idArtikla).size();

        SlikaArtikla slika = new SlikaArtikla();
        slika.setIdArtikla(idArtikla);
        slika.setPutanja(relativnaPutanja);
        slika.setRedosljed(redosljed);
        slika.setJeNaslovna(false);
        slika.setAktivan(true);
        slika.setIdKompanije(idKompanije);

        SlikaArtikla saved = slikaArtiklaRepository.save(slika);
        log.info("Uploadovana slika za artikal id={}, putanja='{}'", idArtikla, relativnaPutanja);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public SlikaArtiklaDTO.ListItemDTO update(Long id, SlikaArtiklaDTO.UpdateDTO dto) {
        SlikaArtikla slika = slikaArtiklaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SlikaArtikla", id));

        slika.setRedosljed(dto.redosljed());
        slika.setJeNaslovna(dto.jeNaslovna());
        if (dto.aktivan() != null) {
            slika.setAktivan(dto.aktivan());
        }

        SlikaArtikla saved = slikaArtiklaRepository.save(slika);
        log.info("Ažurirana slika: id={}", id);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        SlikaArtikla slika = slikaArtiklaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SlikaArtikla", id));

        obrisiSaDiskaAkoPostoji(slika.getPutanja());

        slika.setAktivan(false);
        slikaArtiklaRepository.save(slika);
        log.info("Deaktivirana slika: id={}", id);
    }

    @Override
    @Transactional
    public void reorderSlike(List<SlikaArtiklaDTO.ReorderItemDTO> items) {
        for (SlikaArtiklaDTO.ReorderItemDTO item : items) {
            SlikaArtikla slika = slikaArtiklaRepository.findById(item.id())
                    .orElseThrow(() -> new ResourceNotFoundException("SlikaArtikla", item.id()));
            slika.setRedosljed(item.redosljed());
            slikaArtiklaRepository.save(slika);
        }
        log.info("Reorderovane slike: count={}", items.size());
    }

    // ---- Private helpers ----

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Fajl nije priložen ili je prazan.");
        }

        long maxVelicinaBytes = maxVelicinaeMb * 1024L * 1024L;
        if (file.getSize() > maxVelicinaBytes) {
            throw new BusinessException("Veličina fajla premašuje dozvoljeni maksimum od " + maxVelicinaeMb + " MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !DOZVOLJENI_TIPOVI.contains(contentType)) {
            throw new BusinessException("Nedozvoljeni tip fajla. Dozvoljeni formati: jpg, jpeg, png, webp.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String ekstenzija = extractEkstenzija(originalFilename);
            if (!DOZVOLJENE_EKSTENZIJE.contains(ekstenzija)) {
                throw new BusinessException("Nedozvoljena ekstenzija fajla. Dozvoljene: jpg, jpeg, png, webp.");
            }
        }
    }

    private String spremiNaDisk(MultipartFile file, Long idKompanije, Long idArtikla) {
        String ekstenzija = extractEkstenzija(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "file.jpg"
        );
        String noviNazivFajla = UUID.randomUUID() + "." + ekstenzija;
        String relativniPoddirektorij = idKompanije + "/" + idArtikla;
        String relativnaPutanja = relativniPoddirektorij + "/" + noviNazivFajla;

        Path odredisniDirektorij = Paths.get(uploadPutanja, relativniPoddirektorij);
        Path odredisniPath = odredisniDirektorij.resolve(noviNazivFajla);

        try {
            Files.createDirectories(odredisniDirektorij);
            Files.copy(file.getInputStream(), odredisniPath);
        } catch (IOException e) {
            log.error("Greška pri snimanju slike na disk: {}", e.getMessage(), e);
            throw new BusinessException("Nije moguće snimiti fajl na disk.");
        }

        return relativnaPutanja;
    }

    private void obrisiSaDiskaAkoPostoji(String relativnaPutanja) {
        if (relativnaPutanja == null || relativnaPutanja.isBlank()) {
            return;
        }
        Path path = Paths.get(uploadPutanja, relativnaPutanja);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Nije moguće obrisati fajl sa diska: putanja='{}', greška='{}'", path, e.getMessage());
        }
    }

    private String extractEkstenzija(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return "jpg";
        }
        return filename.substring(lastDot + 1).toLowerCase();
    }

    private SlikaArtiklaDTO.ListItemDTO toDTO(SlikaArtikla s) {
        return new SlikaArtiklaDTO.ListItemDTO(
                s.getId(),
                s.getIdArtikla(),
                s.getPutanja(),
                s.getRedosljed(),
                s.isJeNaslovna(),
                s.isAktivan()
        );
    }
}
