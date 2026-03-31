package ba.maloprodaja.dokumenti.otpremnica.repository;

import ba.maloprodaja.dokumenti.otpremnica.entity.Otpremnica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OtpremnicaRepository extends JpaRepository<Otpremnica, Long> {

    List<Otpremnica> findByIdKompanije(Long idKompanije);

    List<Otpremnica> findByIdKompanijeAndIdPoslovnicePosiljaoca(Long idKompanije, Long idPoslovnicePosiljaoca);

    List<Otpremnica> findByIdKompanijeAndIdPoslovnicePrimaoca(Long idKompanije, Long idPoslovnicePrimaoca);

    boolean existsByIdKompanijeAndBroj(Long idKompanije, String broj);
}
