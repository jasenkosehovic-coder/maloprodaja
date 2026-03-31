package ba.maloprodaja.dokumenti.faktura.repository;

import ba.maloprodaja.dokumenti.enums.StatusFakture;
import ba.maloprodaja.dokumenti.faktura.entity.UlaznaFaktura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UlaznaFakturaRepository extends JpaRepository<UlaznaFaktura, Long> {

    List<UlaznaFaktura> findByIdKompanijeAndIdPoslovnice(Long idKompanije, Long idPoslovnice);

    List<UlaznaFaktura> findByIdKompanijeAndIdPoslovniceAndStatus(Long idKompanije, Long idPoslovnice, StatusFakture status);

    boolean existsByIdKompanijeAndIdPoslovniceAndBroj(Long idKompanije, Long idPoslovnice, String broj);
}
