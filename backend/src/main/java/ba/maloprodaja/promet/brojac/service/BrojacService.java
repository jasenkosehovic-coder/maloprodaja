package ba.maloprodaja.promet.brojac.service;

import ba.maloprodaja.promet.brojac.entity.BrojacDokumenta;
import ba.maloprodaja.promet.brojac.repository.BrojacRepository;
import ba.maloprodaja.promet.tipdokumenta.entity.TipDokumenta;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrojacService {

    private final BrojacRepository brojacRepository;

    /**
     * Atomically increments the counter for the given document type, company and current year.
     * Returns the formatted document number: KOD-YYYY-NNNN (e.g. UF-2026-0001).
     * Must be called within an existing transaction to ensure the lock is held until commit.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public String nextBrojDokumenta(TipDokumenta tipDokumenta, Long idKompanije) {
        short godina = (short) LocalDate.now().getYear();

        BrojacDokumenta brojac = brojacRepository
                .findForUpdate(tipDokumenta.getId(), idKompanije, godina)
                .orElseGet(() -> {
                    BrojacDokumenta novi = new BrojacDokumenta();
                    novi.setTipDokumenta(tipDokumenta);
                    novi.setIdKompanije(idKompanije);
                    novi.setGodina(godina);
                    novi.setBrojac(0);
                    return brojacRepository.save(novi);
                });

        int noviRedni = brojac.getBrojac() + 1;
        brojac.setBrojac(noviRedni);
        brojacRepository.save(brojac);

        String formatted = "%s-%d-%04d".formatted(tipDokumenta.getKod(), godina, noviRedni);
        log.debug("Generisan broj dokumenta: {}", formatted);
        return formatted;
    }
}
