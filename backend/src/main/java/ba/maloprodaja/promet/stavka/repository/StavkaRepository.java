package ba.maloprodaja.promet.stavka.repository;

import ba.maloprodaja.promet.stavka.entity.StavkaDokumenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StavkaRepository extends JpaRepository<StavkaDokumenta, Long> {

    List<StavkaDokumenta> findByDokumentId(Long dokumentId);
}
