package ba.maloprodaja.sifarnici.artikalposl.entity;

import ba.maloprodaja.common.entity.PoslovnicaBaseEntity;
import ba.maloprodaja.sifarnici.artikalkomp.entity.ArtikalKompanija;
import ba.maloprodaja.sifarnici.artikalkomp.entity.TipMarze;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(
        name = "artikli_poslovnice",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_artikal_poslovnica",
                columnNames = {"id_artikla", "id_poslovnice"}
        )
)
public class ArtikalPoslovnica extends PoslovnicaBaseEntity {

    @Column(name = "id_artikla", nullable = false)
    private Long idArtikla;

    @Column(name = "vpc", precision = 15, scale = 4)
    private BigDecimal vpc;

    @Column(name = "marza", precision = 10, scale = 4)
    private BigDecimal marza;

    @Enumerated(EnumType.STRING)
    @Column(name = "tip_marze", nullable = false, length = 30)
    private TipMarze tipMarze = TipMarze.SLOBODNA;

    @Column(name = "mpc", precision = 15, scale = 4)
    private BigDecimal mpc;

    @Column(name = "kolicina", nullable = false, precision = 15, scale = 4)
    private BigDecimal kolicina = BigDecimal.ZERO;

    @Column(name = "min_zaliha", precision = 15, scale = 4)
    private BigDecimal minZaliha;

    @Column(name = "optimalna_zaliha", precision = 15, scale = 4)
    private BigDecimal optimalnaZaliha;

    @Column(name = "aktivan", nullable = false)
    private boolean aktivan = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_artikla", insertable = false, updatable = false)
    private ArtikalKompanija artikalKompanija;
}
