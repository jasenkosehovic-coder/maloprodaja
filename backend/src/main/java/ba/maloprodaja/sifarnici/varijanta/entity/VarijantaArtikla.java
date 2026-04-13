package ba.maloprodaja.sifarnici.varijanta.entity;

import ba.maloprodaja.common.entity.KompanijaBaseEntity;
import ba.maloprodaja.sifarnici.artikalkomp.entity.ArtikalKompanija;
import ba.maloprodaja.sifarnici.boja.entity.Boja;
import ba.maloprodaja.sifarnici.velicina.entity.Velicina;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "varijante_artikla")
public class VarijantaArtikla extends KompanijaBaseEntity {

    @Column(name = "id_artikla", nullable = false)
    private Long idArtikla;

    @Column(name = "id_velicine")
    private Long idVelicine;

    @Column(name = "id_boje")
    private Long idBoje;

    @Column(name = "aktivan", nullable = false)
    private boolean aktivan = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_artikla", insertable = false, updatable = false)
    private ArtikalKompanija artikalKompanija;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_velicine", insertable = false, updatable = false)
    private Velicina velicina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_boje", insertable = false, updatable = false)
    private Boja boja;
}
