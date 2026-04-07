package ba.maloprodaja.sifarnici.artikalkomp.repository;

import ba.maloprodaja.sifarnici.artikalkomp.entity.Barkod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BarkodRepository extends JpaRepository<Barkod, Long> {

    List<Barkod> findByIdKompanije(Long idKompanije);

    List<Barkod> findByIdVarijanteAndAktivanTrue(Long idVarijante);

    List<Barkod> findByIdVarijanteIn(List<Long> idVarijante);

    boolean existsByBarkodAndIdKompanije(String barkod, Long idKompanije);

    boolean existsByBarkodAndIdKompanijeAndIdNot(String barkod, Long idKompanije, Long id);
}
