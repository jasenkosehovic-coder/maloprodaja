package ba.maloprodaja.sifarnici.popust.entity;

import ba.maloprodaja.common.entity.KompanijaBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "popusti")
public class Popust extends KompanijaBaseEntity {

    @Column(name = "naziv", nullable = false, length = 200)
    private String naziv;

    @Column(name = "procenat", nullable = false, precision = 10, scale = 4)
    private BigDecimal procenat;

    @Column(name = "datum_od")
    private LocalDate datumOd;

    @Column(name = "datum_do")
    private LocalDate datumDo;

    @Column(name = "aktivan", nullable = false)
    private boolean aktivan = true;

    @Column(name = "id_poslovnice")
    private Long idPoslovnice;
}
