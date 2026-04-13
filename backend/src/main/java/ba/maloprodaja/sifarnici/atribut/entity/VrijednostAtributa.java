package ba.maloprodaja.sifarnici.atribut.entity;

import ba.maloprodaja.common.entity.KompanijaBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "vrijednosti_atributa")
public class VrijednostAtributa extends KompanijaBaseEntity {

    @Column(name = "id_definicije", nullable = false)
    private Long idDefinicije;

    @Column(name = "vrijednost", nullable = false, length = 200)
    private String vrijednost;

    @Column(name = "redosljed", nullable = false)
    private int redosljed = 0;

    @Column(name = "aktivan", nullable = false)
    private boolean aktivan = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_definicije", insertable = false, updatable = false)
    private DefinicijaAtributa definicijaAtributa;
}
