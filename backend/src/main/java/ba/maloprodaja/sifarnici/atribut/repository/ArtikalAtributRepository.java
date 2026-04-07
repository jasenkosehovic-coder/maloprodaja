package ba.maloprodaja.sifarnici.atribut.repository;

import ba.maloprodaja.sifarnici.atribut.entity.ArtikalAtribut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArtikalAtributRepository extends JpaRepository<ArtikalAtribut, Long> {

    List<ArtikalAtribut> findByIdArtikla(Long idArtikla);

    List<ArtikalAtribut> findByIdArtiklaInAndIdKompanije(Collection<Long> idArtikala, Long idKompanije);

    Optional<ArtikalAtribut> findByIdArtiklaAndIdDefinicije(Long idArtikla, Long idDefinicije);

    void deleteByIdArtiklaAndIdDefinicijeNotIn(Long idArtikla, List<Long> idDefinicijeList);

    void deleteByIdArtikla(Long idArtikla);
}
