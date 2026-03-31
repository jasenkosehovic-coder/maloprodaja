package ba.maloprodaja.dokumenti.otpremnica.repository;

import ba.maloprodaja.dokumenti.otpremnica.entity.OtpremnicaStavka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OtpremnicaStavkaRepository extends JpaRepository<OtpremnicaStavka, Long> {

    List<OtpremnicaStavka> findByOtpremnicaId(Long otpremnicaId);

    List<OtpremnicaStavka> findByOtpremnicaIdIn(List<Long> otpremnicaIds);
}
