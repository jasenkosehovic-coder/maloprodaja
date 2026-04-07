package ba.maloprodaja.sifarnici.velicina.repository;

import ba.maloprodaja.sifarnici.velicina.entity.Velicina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VelicinaRepository extends JpaRepository<Velicina, Long> {

    List<Velicina> findByIdTipaVelicinaOrderByRedosljed(Long idTipaVelicina);

    boolean existsByIdTipaVelicinaAndAktivanTrue(Long idTipaVelicina);

    boolean existsByIdTipaVelicinaAndOznakaAndIdNot(Long idTipaVelicina, String oznaka, Long id);
}
