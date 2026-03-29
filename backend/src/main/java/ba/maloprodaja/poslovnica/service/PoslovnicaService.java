package ba.maloprodaja.poslovnica.service;

import ba.maloprodaja.poslovnica.dto.PoslovnicaDTO;
import ba.maloprodaja.poslovnica.repository.PoslovnicaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PoslovnicaService implements IPoslovnicaService {

    private final PoslovnicaRepository poslovnicaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PoslovnicaDTO> listByKompanija(Long idKompanije) {
        log.debug("Fetching active poslovnice for idKompanije={}", idKompanije);
        return poslovnicaRepository.findByIdKompanije(idKompanije).stream()
                .filter(p -> p.isAktivan())
                .map(p -> new PoslovnicaDTO(p.getId(), p.getNaziv(), p.getAdresa(), p.getGrad(), p.getIdKompanije()))
                .toList();
    }
}
