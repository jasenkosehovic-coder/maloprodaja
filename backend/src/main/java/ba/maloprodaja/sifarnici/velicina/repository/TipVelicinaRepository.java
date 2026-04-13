package ba.maloprodaja.sifarnici.velicina.repository;

import ba.maloprodaja.sifarnici.velicina.entity.TipVelicina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipVelicinaRepository extends JpaRepository<TipVelicina, Long> {

    List<TipVelicina> findByIdKompanijeOrderByNazivAsc(Long idKompanije);

    boolean existsByNazivAndIdKompanije(String naziv, Long idKompanije);

    boolean existsByNazivAndIdKompanijeAndIdNot(String naziv, Long idKompanije, Long id);
}
