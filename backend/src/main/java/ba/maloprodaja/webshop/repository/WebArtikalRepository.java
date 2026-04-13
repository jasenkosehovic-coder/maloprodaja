package ba.maloprodaja.webshop.repository;

import ba.maloprodaja.webshop.entity.WebArtikal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebArtikalRepository extends JpaRepository<WebArtikal, Long> {

    List<WebArtikal> findByIdKompanijeOrderByIdDesc(Long idKompanije);

    Optional<WebArtikal> findByIdArtiklaAndIdKompanije(Long idArtikla, Long idKompanije);
}
