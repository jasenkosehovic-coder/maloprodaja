package ba.maloprodaja.sifarnici.artikalposl.repository;

import ba.maloprodaja.sifarnici.artikalposl.entity.ArtikalPoslovnica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface ArtikalPoslovnicaRepository extends JpaRepository<ArtikalPoslovnica, Long> {

    List<ArtikalPoslovnica> findByIdPoslovnice(Long idPoslovnice);

    List<ArtikalPoslovnica> findByIdArtikla(Long idArtikla);

    List<ArtikalPoslovnica> findByIdKompanije(Long idKompanije);

    Optional<ArtikalPoslovnica> findByIdArtiklaAndIdPoslovnice(Long idArtikla, Long idPoslovnice);

    boolean existsByIdArtiklaAndIdPoslovnice(Long idArtikla, Long idPoslovnice);

    /**
     * Returns the total stock (sum of all variant quantities) per artikal-poslovnica record
     * for a given poslovnica. One query — no N+1.
     *
     * Result: Object[0] = ap.id (Long), Object[1] = SUM(vap.kolicina) (BigDecimal, never null)
     */
    @Query("""
            SELECT ap.id, COALESCE(SUM(vap.kolicina), 0)
            FROM ArtikalPoslovnica ap
            LEFT JOIN VarijantaArtikla va ON va.idArtikla = ap.idArtikla AND va.aktivan = true
            LEFT JOIN VarijantaArtiklaPoslovnica vap ON vap.idVarijante = va.id AND vap.idPoslovnice = ap.idPoslovnice
            WHERE ap.idPoslovnice = :idPoslovnice
            GROUP BY ap.id
            """)
    List<Object[]> sumKolicinaByPoslovnica(@Param("idPoslovnice") Long idPoslovnice);

    /**
     * Returns the total stock per artikal-poslovnica record for all poslovnice that carry
     * a given artikal. One query — no N+1.
     *
     * Result: Object[0] = ap.id (Long), Object[1] = SUM(vap.kolicina) (BigDecimal, never null)
     */
    @Query("""
            SELECT ap.id, COALESCE(SUM(vap.kolicina), 0)
            FROM ArtikalPoslovnica ap
            LEFT JOIN VarijantaArtikla va ON va.idArtikla = ap.idArtikla AND va.aktivan = true
            LEFT JOIN VarijantaArtiklaPoslovnica vap ON vap.idVarijante = va.id AND vap.idPoslovnice = ap.idPoslovnice
            WHERE ap.idArtikla = :idArtikla
            GROUP BY ap.id
            """)
    List<Object[]> sumKolicinaByArtikla(@Param("idArtikla") Long idArtikla);

    /**
     * Convenience default method that converts the raw Object[] list into a Map keyed by ap.id.
     */
    default Map<Long, BigDecimal> ukupnaKolicinaMapByPoslovnica(Long idPoslovnice) {
        return toKolicinaMap(sumKolicinaByPoslovnica(idPoslovnice));
    }

    /**
     * Convenience default method that converts the raw Object[] list into a Map keyed by ap.id.
     */
    default Map<Long, BigDecimal> ukupnaKolicinaMapByArtikla(Long idArtikla) {
        return toKolicinaMap(sumKolicinaByArtikla(idArtikla));
    }

    private static Map<Long, BigDecimal> toKolicinaMap(List<Object[]> rows) {
        return rows.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (BigDecimal) row[1]
                ));
    }
}
