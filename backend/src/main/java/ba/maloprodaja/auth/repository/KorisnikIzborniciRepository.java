package ba.maloprodaja.auth.repository;

import ba.maloprodaja.auth.entity.KorisnikIzbornik;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface KorisnikIzborniciRepository extends JpaRepository<KorisnikIzbornik, Long> {

    List<KorisnikIzbornik> findByKorisnikId(Long korisnikId);

    List<KorisnikIzbornik> findByKorisnikIdIn(java.util.Collection<Long> korisnikIds);

    long countByKorisnikId(Long korisnikId);

    @Query("SELECT DISTINCT k.izbornikKljuc FROM KorisnikIzbornik k ORDER BY k.izbornikKljuc")
    List<String> findDistinctIzbornikKljucevi();

    @Modifying
    @Transactional
    @Query("DELETE FROM KorisnikIzbornik k WHERE k.korisnikId = :korisnikId")
    void deleteByKorisnikId(@Param("korisnikId") Long korisnikId);
}
