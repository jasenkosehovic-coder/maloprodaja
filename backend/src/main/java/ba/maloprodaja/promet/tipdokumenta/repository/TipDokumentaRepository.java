package ba.maloprodaja.promet.tipdokumenta.repository;

import ba.maloprodaja.promet.tipdokumenta.entity.TipDokumenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipDokumentaRepository extends JpaRepository<TipDokumenta, Long> {

    List<TipDokumenta> findByIdKompanije(Long idKompanije);

    Optional<TipDokumenta> findByKodAndIdKompanije(String kod, Long idKompanije);

    boolean existsByKodAndIdKompanije(String kod, Long idKompanije);
}
