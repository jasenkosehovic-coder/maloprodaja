package ba.maloprodaja.promet.brojac.repository;

import ba.maloprodaja.promet.brojac.entity.BrojacDokumenta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrojacRepository extends JpaRepository<BrojacDokumenta, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BrojacDokumenta b WHERE b.tipDokumenta.id = :idTipa AND b.idKompanije = :idKompanije AND b.godina = :godina")
    Optional<BrojacDokumenta> findForUpdate(
            @Param("idTipa") Long idTipa,
            @Param("idKompanije") Long idKompanije,
            @Param("godina") Short godina
    );
}
