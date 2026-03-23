package ba.maloprodaja.auth.repository;

import ba.maloprodaja.auth.entity.Korisnik;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KorisnikRepository extends JpaRepository<Korisnik, Long> {

    Optional<Korisnik> findByUsername(String username);
}
