package ba.maloprodaja.sifarnici.atribut.repository;

import ba.maloprodaja.sifarnici.atribut.entity.VrijednostAtributa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VrijednostAtributaRepository extends JpaRepository<VrijednostAtributa, Long> {

    List<VrijednostAtributa> findByIdDefinicijeOrderByRedosljed(Long idDefinicije);

    boolean existsByIdDefinicijeAndVrijednostAndIdNot(Long idDefinicije, String vrijednost, Long id);

    Optional<VrijednostAtributa> findByIdAndIdDefinicije(Long id, Long idDefinicije);
}
