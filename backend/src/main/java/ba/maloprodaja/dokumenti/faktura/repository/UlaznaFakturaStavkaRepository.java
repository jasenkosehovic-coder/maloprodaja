package ba.maloprodaja.dokumenti.faktura.repository;

import ba.maloprodaja.dokumenti.faktura.entity.UlaznaFakturaStavka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UlaznaFakturaStavkaRepository extends JpaRepository<UlaznaFakturaStavka, Long> {

    List<UlaznaFakturaStavka> findByFakturaId(Long fakturaId);
}
