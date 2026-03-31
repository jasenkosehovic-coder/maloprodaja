package ba.maloprodaja.dokumenti.nivelacija.repository;

import ba.maloprodaja.dokumenti.nivelacija.entity.NivelacijaStavka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NivelacijaStavkaRepository extends JpaRepository<NivelacijaStavka, Long> {

    List<NivelacijaStavka> findByNivelacijaId(Long nivelacijaId);

    List<NivelacijaStavka> findByNivelacijaIdIn(List<Long> nivelacijaIds);
}
