package ba.maloprodaja.dokumenti.otpremnica.repository;

import ba.maloprodaja.dokumenti.otpremnica.entity.Otpremnica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OtpremnicaRepository extends JpaRepository<Otpremnica, Long> {

    List<Otpremnica> findByIdKompanije(Long idKompanije);

    List<Otpremnica> findByIdKompanijeAndIdPoslovnicePosiljaoca(Long idKompanije, Long idPoslovnicePosiljaoca);

    List<Otpremnica> findByIdKompanijeAndIdPoslovnicePosiljaocaAndGodina(Long idKompanije, Long idPoslovnicePosiljaoca, Integer godina);

    List<Otpremnica> findByIdKompanijeAndIdPoslovnicePrimaoca(Long idKompanije, Long idPoslovnicePrimaoca);

    @Query("SELECT o FROM Otpremnica o WHERE o.idKompanije = :idKompanije AND o.idPoslovnicePrimaoca = :idPoslovnicePrimaoca AND o.godina = :godina")
    List<Otpremnica> findByIdKompanijeAndIdPoslovnicePrimaoсaAndGodina(
            @Param("idKompanije") Long idKompanije,
            @Param("idPoslovnicePrimaoca") Long idPoslovnicePrimaoca,
            @Param("godina") Integer godina);

    boolean existsByIdKompanijeAndBroj(Long idKompanije, String broj);
}
