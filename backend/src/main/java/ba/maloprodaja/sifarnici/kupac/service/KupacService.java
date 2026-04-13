package ba.maloprodaja.sifarnici.kupac.service;

import ba.maloprodaja.common.exception.ResourceNotFoundException;
import ba.maloprodaja.sifarnici.kupac.dto.KupacDTO;
import ba.maloprodaja.sifarnici.kupac.entity.Kupac;
import ba.maloprodaja.sifarnici.kupac.repository.KupacRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KupacService implements IKupacService {

    private final KupacRepository kupacRepository;

    @Override
    public List<KupacDTO.ListItemDTO> listAll(Long idKompanije) {
        return kupacRepository.findByIdKompanijeOrderByNazivAsc(idKompanije)
                .stream()
                .map(this::toListItemDTO)
                .toList();
    }

    @Override
    @Transactional
    public KupacDTO.ListItemDTO create(KupacDTO.CreateDTO dto, Long idKompanije) {
        Kupac k = new Kupac();
        k.setNaziv(dto.naziv());
        k.setAdresa(dto.adresa());
        k.setGrad(dto.grad());
        k.setTelefon(dto.telefon());
        k.setEmail(dto.email());
        k.setPib(dto.pib());
        k.setAktivan(true);
        k.setIdKompanije(idKompanije);

        Kupac saved = kupacRepository.save(k);
        log.info("Kreiran novi kupac: naziv='{}', idKompanije={}", saved.getNaziv(), idKompanije);
        return toListItemDTO(saved);
    }

    @Override
    @Transactional
    public KupacDTO.ListItemDTO update(Long id, KupacDTO.UpdateDTO dto) {
        Kupac k = kupacRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kupac", id));

        k.setNaziv(dto.naziv());
        k.setAdresa(dto.adresa());
        k.setGrad(dto.grad());
        k.setTelefon(dto.telefon());
        k.setEmail(dto.email());
        k.setPib(dto.pib());
        if (dto.aktivan() != null) {
            k.setAktivan(dto.aktivan());
        }

        Kupac saved = kupacRepository.save(k);
        log.info("Ažuriran kupac: id={}", id);
        return toListItemDTO(saved);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Kupac k = kupacRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kupac", id));
        k.setAktivan(false);
        kupacRepository.save(k);
        log.info("Deaktiviran kupac: id={}", id);
    }

    private KupacDTO.ListItemDTO toListItemDTO(Kupac k) {
        return new KupacDTO.ListItemDTO(
                k.getId(),
                k.getNaziv(),
                k.getAdresa(),
                k.getGrad(),
                k.getTelefon(),
                k.getEmail(),
                k.getPib(),
                k.isAktivan()
        );
    }
}
