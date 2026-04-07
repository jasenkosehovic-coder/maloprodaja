package ba.maloprodaja.sifarnici.varijanta.entity;

import ba.maloprodaja.common.entity.KompanijaBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "varijante_artikla_poslovnica")
public class VarijantaArtiklaPoslovnica extends KompanijaBaseEntity {

    @Column(name = "id_varijante", nullable = false)
    private Long idVarijante;

    @Column(name = "id_poslovnice", nullable = false)
    private Long idPoslovnice;

    @Column(name = "kolicina", nullable = false, precision = 15, scale = 4)
    private BigDecimal kolicina = BigDecimal.ZERO;

    @Column(name = "min_zaliha", precision = 15, scale = 4)
    private BigDecimal minZaliha;

    @Column(name = "optimalna_zaliha", precision = 15, scale = 4)
    private BigDecimal optimalnaZaliha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_varijante", insertable = false, updatable = false)
    private VarijantaArtikla varijantaArtikla;
}
