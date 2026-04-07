package ba.maloprodaja.sifarnici.slika.repository;

import ba.maloprodaja.sifarnici.slika.entity.SlikaArtikla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlikaArtiklaRepository extends JpaRepository<SlikaArtikla, Long> {

    List<SlikaArtikla> findByIdArtiklaOrderByRedosljed(Long idArtikla);
}
