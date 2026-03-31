package ba.maloprodaja.sifarnici.artikalkomp.entity;

import ba.maloprodaja.common.entity.KompanijaBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "barkodovi")
public class Barkod extends KompanijaBaseEntity {

    @Column(name = "barkod", nullable = false, length = 100)
    private String barkod;

    @Column(name = "id_artikla", nullable = false)
    private Long idArtikla;

    @Column(name = "id_poslovnice")
    private Long idPoslovnice;

    @Column(name = "aktivan", nullable = false)
    private boolean aktivan = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_artikla", insertable = false, updatable = false)
    private ArtikalKompanija artikal;
}
