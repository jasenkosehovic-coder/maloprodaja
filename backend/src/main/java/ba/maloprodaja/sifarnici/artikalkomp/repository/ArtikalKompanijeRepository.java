package ba.maloprodaja.sifarnici.artikalkomp.repository;

import ba.maloprodaja.sifarnici.artikalkomp.entity.ArtikalKompanija;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtikalKompanijeRepository extends JpaRepository<ArtikalKompanija, Long> {

    List<ArtikalKompanija> findByIdKompanije(Long idKompanije);

    boolean existsBySifraAndIdKompanije(String sifra, Long idKompanije);

    Optional<ArtikalKompanija> findBySifraAndIdKompanije(String sifra, Long idKompanije);
}
