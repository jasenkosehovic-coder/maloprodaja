package ba.maloprodaja.promet.tipdokumenta.service;

import ba.maloprodaja.promet.tipdokumenta.dto.TipDokumentaDTO;
import ba.maloprodaja.promet.tipdokumenta.repository.TipDokumentaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TipDokumentaService implements ITipDokumentaService {

    private final TipDokumentaRepository tipDokumentaRepository;

    @Override
    public List<TipDokumentaDTO> findAll(Long idKompanije) {
        return tipDokumentaRepository.findByIdKompanije(idKompanije).stream()
                .map(t -> new TipDokumentaDTO(t.getId(), t.getKod(), t.getNaziv(), t.getSmjerKolicine()))
                .toList();
    }
}
