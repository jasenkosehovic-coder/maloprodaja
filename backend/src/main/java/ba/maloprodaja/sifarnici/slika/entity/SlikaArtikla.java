package ba.maloprodaja.sifarnici.slika.entity;

import ba.maloprodaja.common.entity.KompanijaBaseEntity;
import ba.maloprodaja.sifarnici.artikalkomp.entity.ArtikalKompanija;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "slike_artikala")
public class SlikaArtikla extends KompanijaBaseEntity {

    @Column(name = "id_artikla", nullable = false)
    private Long idArtikla;

    @Column(name = "putanja", nullable = false, length = 500)
    private String putanja;

    @Column(name = "redosljed", nullable = false)
    private int redosljed = 0;

    @Column(name = "je_naslovna", nullable = false)
    private boolean jeNaslovna = false;

    @Column(name = "aktivan", nullable = false)
    private boolean aktivan = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_artikla", insertable = false, updatable = false)
    private ArtikalKompanija artikal;
}
