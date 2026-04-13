package ba.maloprodaja.sifarnici.velicina.entity;

import ba.maloprodaja.common.entity.KompanijaBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tipovi_velicina")
public class TipVelicina extends KompanijaBaseEntity {

    @Column(name = "naziv", nullable = false, length = 100)
    private String naziv;

    @Column(name = "opis", length = 300)
    private String opis;

    @Column(name = "aktivan", nullable = false)
    private boolean aktivan = true;
}
