package ba.maloprodaja.sifarnici.dobavljac.repository;

import ba.maloprodaja.sifarnici.dobavljac.entity.Dobavljac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DobavljacRepository extends JpaRepository<Dobavljac, Long> {

    List<Dobavljac> findByIdKompanije(Long idKompanije);
}
