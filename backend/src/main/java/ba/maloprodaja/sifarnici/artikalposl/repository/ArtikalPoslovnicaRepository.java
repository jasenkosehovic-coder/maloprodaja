package ba.maloprodaja.sifarnici.artikalposl.repository;

import ba.maloprodaja.sifarnici.artikalposl.entity.ArtikalPoslovnica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtikalPoslovnicaRepository extends JpaRepository<ArtikalPoslovnica, Long> {

    List<ArtikalPoslovnica> findByIdPoslovnice(Long idPoslovnice);

    List<ArtikalPoslovnica> findByIdArtikla(Long idArtikla);

    Optional<ArtikalPoslovnica> findByIdArtiklaAndIdPoslovnice(Long idArtikla, Long idPoslovnice);

    boolean existsByIdArtiklaAndIdPoslovnice(Long idArtikla, Long idPoslovnice);
}
