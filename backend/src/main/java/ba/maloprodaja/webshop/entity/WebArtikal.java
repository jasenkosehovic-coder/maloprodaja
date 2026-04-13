package ba.maloprodaja.webshop.entity;

import ba.maloprodaja.common.entity.KompanijaBaseEntity;
import ba.maloprodaja.sifarnici.artikalkomp.entity.ArtikalKompanija;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(
        name = "web_artikli",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_artikla", "id_kompanije"})
)
public class WebArtikal extends KompanijaBaseEntity {

    @Column(name = "id_artikla", nullable = false)
    private Long idArtikla;

    @Column(name = "web_naziv", length = 300)
    private String webNaziv;

    @Column(name = "web_opis", columnDefinition = "TEXT")
    private String webOpis;

    @Column(name = "aktivan", nullable = false)
    private Boolean aktivan = true;

    @Column(name = "mpc", precision = 15, scale = 4)
    private BigDecimal mpc;

    @Column(name = "popust", precision = 10, scale = 4)
    private BigDecimal popust;

    @Column(name = "nova_mpc", precision = 15, scale = 4)
    private BigDecimal novaMpc;

    @Column(name = "meta_title", length = 160)
    private String metaTitle;

    @Column(name = "meta_opis", length = 320)
    private String metaOpis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_artikla", insertable = false, updatable = false)
    private ArtikalKompanija artikal;
}
