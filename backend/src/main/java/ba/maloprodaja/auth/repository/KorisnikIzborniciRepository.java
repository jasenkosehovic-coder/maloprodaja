package ba.maloprodaja.auth.repository;

import ba.maloprodaja.auth.entity.KorisnikIzbornik;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KorisnikIzborniciRepository extends JpaRepository<KorisnikIzbornik, Long> {

    List<KorisnikIzbornik> findByKorisnikId(Long korisnikId);

    void deleteByKorisnikId(Long korisnikId);
}
