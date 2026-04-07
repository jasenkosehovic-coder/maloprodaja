package ba.maloprodaja.sifarnici.popust.repository;

import ba.maloprodaja.sifarnici.popust.entity.Popust;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PopustRepository extends JpaRepository<Popust, Long> {

    List<Popust> findByIdKompanijeOrderByNazivAsc(Long idKompanije);
}
