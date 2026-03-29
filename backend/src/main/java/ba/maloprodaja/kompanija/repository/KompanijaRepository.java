package ba.maloprodaja.kompanija.repository;

import ba.maloprodaja.kompanija.entity.Kompanija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KompanijaRepository extends JpaRepository<Kompanija, Long> {

    Optional<Kompanija> findByNaziv(String naziv);

    Optional<Kompanija> findByPib(String pib);
}
