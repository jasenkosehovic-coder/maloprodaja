package ba.maloprodaja.sifarnici.atribut.repository;

import ba.maloprodaja.sifarnici.atribut.entity.DefinicijaAtributa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefinicijaAtributaRepository extends JpaRepository<DefinicijaAtributa, Long> {

    List<DefinicijaAtributa> findByIdKompanijeOrderByRedosljed(Long idKompanije);

    boolean existsByNazivAndIdKompanije(String naziv, Long idKompanije);

    boolean existsByNazivAndIdKompanijeAndIdNot(String naziv, Long idKompanije, Long id);
}
