package ba.maloprodaja.sifarnici.proizvodjac.repository;

import ba.maloprodaja.sifarnici.proizvodjac.entity.Proizvodjac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProizvodjacRepository extends JpaRepository<Proizvodjac, Long> {

    List<Proizvodjac> findByIdKompanije(Long idKompanije);
}
