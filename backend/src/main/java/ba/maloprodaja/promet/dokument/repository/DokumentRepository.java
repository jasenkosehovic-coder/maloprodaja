package ba.maloprodaja.promet.dokument.repository;

import ba.maloprodaja.promet.dokument.entity.Dokument;
import ba.maloprodaja.promet.dokument.entity.StatusDokumenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface DokumentRepository extends JpaRepository<Dokument, Long> {

    @Query("""
            SELECT d FROM Dokument d
            WHERE d.idKompanije = :idKompanije
              AND (:tipKod IS NULL OR d.tipDokumenta.kod = :tipKod)
              AND (:idPoslovnice IS NULL OR d.idPoslovnice = :idPoslovnice)
              AND (:status IS NULL OR d.status = :status)
              AND (:datumOd IS NULL OR d.datum >= :datumOd)
              AND (:datumDo IS NULL OR d.datum <= :datumDo)
            ORDER BY d.datum DESC, d.id DESC
            """)
    Page<Dokument> findByFilter(
            @Param("idKompanije") Long idKompanije,
            @Param("tipKod") String tipKod,
            @Param("idPoslovnice") Long idPoslovnice,
            @Param("status") StatusDokumenta status,
            @Param("datumOd") LocalDate datumOd,
            @Param("datumDo") LocalDate datumDo,
            Pageable pageable
    );
}
