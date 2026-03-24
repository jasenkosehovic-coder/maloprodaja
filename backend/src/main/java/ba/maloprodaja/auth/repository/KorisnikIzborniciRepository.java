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

    @Modifying
    @Transactional
    @Query("DELETE FROM KorisnikIzbornik k WHERE k.korisnikId = :korisnikId")
    void deleteByKorisnikId(@Param("korisnikId") Long korisnikId);
}
