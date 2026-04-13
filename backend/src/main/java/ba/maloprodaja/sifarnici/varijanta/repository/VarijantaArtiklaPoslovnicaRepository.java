package ba.maloprodaja.sifarnici.varijanta.repository;

import ba.maloprodaja.sifarnici.varijanta.entity.VarijantaArtiklaPoslovnica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VarijantaArtiklaPoslovnicaRepository extends JpaRepository<VarijantaArtiklaPoslovnica, Long> {

    List<VarijantaArtiklaPoslovnica> findByIdVarijante(Long idVarijante);

    Optional<VarijantaArtiklaPoslovnica> findByIdVarijanteAndIdPoslovnice(Long idVarijante, Long idPoslovnice);

    List<VarijantaArtiklaPoslovnica> findByIdPoslovniceAndIdKompanije(Long idPoslovnice, Long idKompanije);
}
