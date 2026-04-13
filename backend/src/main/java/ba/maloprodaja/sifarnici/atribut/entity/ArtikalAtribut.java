package ba.maloprodaja.sifarnici.atribut.entity;

import ba.maloprodaja.common.entity.KompanijaBaseEntity;
import ba.maloprodaja.sifarnici.artikalkomp.entity.ArtikalKompanija;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "artikal_atributi")
public class ArtikalAtribut extends KompanijaBaseEntity {

    @Column(name = "id_artikla", nullable = false)
    private Long idArtikla;

    @Column(name = "id_definicije", nullable = false)
    private Long idDefinicije;

    @Column(name = "id_vrijednosti", nullable = false)
    private Long idVrijednosti;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_artikla", insertable = false, updatable = false)
    private ArtikalKompanija artikal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_definicije", insertable = false, updatable = false)
    private DefinicijaAtributa definicijaAtributa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vrijednosti", insertable = false, updatable = false)
    private VrijednostAtributa vrijednostAtributa;
}
