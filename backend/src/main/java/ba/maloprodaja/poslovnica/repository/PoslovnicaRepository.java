package ba.maloprodaja.poslovnica.repository;

import ba.maloprodaja.poslovnica.entity.Poslovnica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PoslovnicaRepository extends JpaRepository<Poslovnica, Long> {

    List<Poslovnica> findByIdKompanije(Long idKompanije);

    Optional<Poslovnica> findByNazivAndIdKompanije(String naziv, Long idKompanije);
}
