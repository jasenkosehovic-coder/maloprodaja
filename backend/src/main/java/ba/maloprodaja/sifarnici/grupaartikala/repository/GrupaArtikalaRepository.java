package ba.maloprodaja.sifarnici.grupaartikala.repository;

import ba.maloprodaja.sifarnici.grupaartikala.entity.GrupaArtikala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrupaArtikalaRepository extends JpaRepository<GrupaArtikala, Long> {

    List<GrupaArtikala> findByIdKompanije(Long idKompanije);
}
