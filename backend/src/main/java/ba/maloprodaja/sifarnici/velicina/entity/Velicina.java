package ba.maloprodaja.sifarnici.velicina.entity;

import ba.maloprodaja.common.entity.KompanijaBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "velicine")
public class Velicina extends KompanijaBaseEntity {

    @Column(name = "id_tipa_velicina", nullable = false)
    private Long idTipaVelicina;

    @Column(name = "oznaka", nullable = false, length = 20)
    private String oznaka;

    @Column(name = "redosljed", nullable = false)
    private int redosljed = 0;

    @Column(name = "aktivan", nullable = false)
    private boolean aktivan = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipa_velicina", insertable = false, updatable = false)
    private TipVelicina tipVelicina;
}
