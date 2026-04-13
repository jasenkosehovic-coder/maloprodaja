package ba.maloprodaja.sifarnici.boja.repository;

import ba.maloprodaja.sifarnici.boja.entity.Boja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BojaRepository extends JpaRepository<Boja, Long> {

    List<Boja> findByIdKompanijeAndAktivanTrueOrderByNazivAsc(Long idKompanije);

    List<Boja> findByIdKompanijeOrderByNazivAsc(Long idKompanije);

    boolean existsByNazivAndIdKompanije(String naziv, Long idKompanije);

    boolean existsByNazivAndIdKompanijeAndIdNot(String naziv, Long idKompanije, Long id);
}
