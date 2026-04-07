package ba.maloprodaja.sifarnici.atribut.entity;

import ba.maloprodaja.common.entity.KompanijaBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "definicije_atributa")
public class DefinicijaAtributa extends KompanijaBaseEntity {

    @Column(name = "naziv", nullable = false, length = 100)
    private String naziv;

    @Column(name = "redosljed", nullable = false)
    private int redosljed = 0;

    @Column(name = "obavezno", nullable = false)
    private boolean obavezno = false;

    @Column(name = "za_web", nullable = false)
    private boolean zaWeb = false;

    @Column(name = "aktivan", nullable = false)
    private boolean aktivan = true;
}
