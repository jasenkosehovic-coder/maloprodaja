package ba.maloprodaja.sifarnici.kupac.repository;

import ba.maloprodaja.sifarnici.kupac.entity.Kupac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KupacRepository extends JpaRepository<Kupac, Long> {

    List<Kupac> findByIdKompanijeOrderByNazivAsc(Long idKompanije);
}
