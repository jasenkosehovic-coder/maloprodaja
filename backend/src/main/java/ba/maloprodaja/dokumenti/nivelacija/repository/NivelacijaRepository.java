package ba.maloprodaja.dokumenti.nivelacija.repository;

import ba.maloprodaja.dokumenti.nivelacija.entity.Nivelacija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NivelacijaRepository extends JpaRepository<Nivelacija, Long> {

    List<Nivelacija> findByIdKompanijeAndIdPoslovnice(Long idKompanije, Long idPoslovnice);

    boolean existsByIdKompanijeAndIdPoslovniceAndBroj(Long idKompanije, Long idPoslovnice, String broj);

    @Query("SELECT COUNT(n) FROM Nivelacija n WHERE n.idKompanije = :idKompanije AND n.idPoslovnice = :idPoslovnice AND YEAR(n.datum) = :godina")
    long countByIdKompanijeAndIdPoslovniceAndGodina(
            @Param("idKompanije") Long idKompanije,
            @Param("idPoslovnice") Long idPoslovnice,
            @Param("godina") int godina
    );
}
