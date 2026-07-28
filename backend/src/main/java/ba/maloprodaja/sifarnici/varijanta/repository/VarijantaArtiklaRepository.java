package ba.maloprodaja.sifarnici.varijanta.repository;

import ba.maloprodaja.sifarnici.varijanta.entity.VarijantaArtikla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface VarijantaArtiklaRepository extends JpaRepository<VarijantaArtikla, Long> {

    List<VarijantaArtikla> findByIdArtikla(Long idArtikla);

    @Query("SELECT v FROM VarijantaArtikla v WHERE v.idArtikla = :idArtikla AND v.idVelicine IS NULL AND v.idBoje IS NULL")
    Optional<VarijantaArtikla> findDefaultByIdArtikla(@Param("idArtikla") Long idArtikla);

    @Query("""
            SELECT COUNT(v) > 0 FROM VarijantaArtikla v
            WHERE v.idArtikla = :idArtikla
              AND (v.idVelicine IS NULL AND :idVelicine IS NULL OR v.idVelicine = :idVelicine)
              AND (v.idBoje IS NULL AND :idBoje IS NULL OR v.idBoje = :idBoje)
            """)
    boolean existsByIdArtiklaAndIdVelicinaAndIdBoje(
            @Param("idArtikla") Long idArtikla,
            @Param("idVelicine") Long idVelicine,
            @Param("idBoje") Long idBoje
    );

    @Query("SELECT COUNT(v) > 0 FROM VarijantaArtikla v WHERE v.idArtikla = :idArtikla AND v.idVelicine IS NULL AND v.idBoje IS NULL")
    boolean existsDefaultByIdArtikla(@Param("idArtikla") Long idArtikla);

    @Query("SELECT COUNT(v) > 0 FROM VarijantaArtikla v WHERE v.idArtikla = :idArtikla AND v.idVelicine IS NOT NULL AND v.aktivan = true")
    boolean existsActiveWithVelicinaByIdArtikla(@Param("idArtikla") Long idArtikla);

    boolean existsByIdBojeAndAktivanTrue(Long idBoje);

    long countByIdArtikla(Long idArtikla);

    @Query("SELECT v FROM VarijantaArtikla v LEFT JOIN FETCH v.artikalKompanija LEFT JOIN FETCH v.velicina LEFT JOIN FETCH v.boja WHERE v.id IN :ids")
    List<VarijantaArtikla> findAllWithDetailsByIdIn(@Param("ids") Collection<Long> ids);
}
