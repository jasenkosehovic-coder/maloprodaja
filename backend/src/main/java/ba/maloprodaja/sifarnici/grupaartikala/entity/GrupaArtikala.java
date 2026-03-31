package ba.maloprodaja.sifarnici.grupaartikala.entity;

import ba.maloprodaja.common.entity.KompanijaBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "grupe_artikala")
public class GrupaArtikala extends KompanijaBaseEntity {

    @Column(name = "naziv", nullable = false, length = 200)
    private String naziv;

    @Column(name = "opis", length = 500)
    private String opis;

    @Column(name = "aktivan", nullable = false)
    private boolean aktivan = true;

    @Column(name = "id_roditeljske_grupe")
    private Long idRoditeljskeGrupe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_roditeljske_grupe", insertable = false, updatable = false)
    private GrupaArtikala roditeljskaGrupa;
}
