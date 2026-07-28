package ba.maloprodaja.promet.stavka.entity;

import ba.maloprodaja.common.entity.KompanijaBaseEntity;
import ba.maloprodaja.promet.dokument.entity.Dokument;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "stavke_dokumenata")
public class StavkaDokumenta extends KompanijaBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_dokumenta", nullable = false)
    private Dokument dokument;

    @Column(name = "id_varijante", nullable = false)
    private Long idVarijante;

    @Column(name = "kolicina", nullable = false, precision = 12, scale = 3)
    private BigDecimal kolicina;

    @Column(name = "vpc", nullable = false, precision = 12, scale = 4)
    private BigDecimal vpc;

    @Column(name = "mpc", nullable = false, precision = 12, scale = 4)
    private BigDecimal mpc = BigDecimal.ZERO;

    @Column(name = "pdv_procenat", nullable = false, precision = 5, scale = 2)
    private BigDecimal pdvProcenat = BigDecimal.ZERO;

    @Column(name = "marza_procenat", nullable = false, precision = 5, scale = 2)
    private BigDecimal marzaProcenat = BigDecimal.ZERO;

    @Column(name = "popust_procenat", nullable = false, precision = 5, scale = 2)
    private BigDecimal popustProcenat = BigDecimal.ZERO;

    @Column(name = "iznos_vpc", nullable = false, precision = 12, scale = 4)
    private BigDecimal iznosVpc = BigDecimal.ZERO;

    @Column(name = "iznos_mpc", nullable = false, precision = 12, scale = 4)
    private BigDecimal iznosMpc = BigDecimal.ZERO;

    @Column(name = "iznos_marze", nullable = false, precision = 12, scale = 4)
    private BigDecimal iznosMarze = BigDecimal.ZERO;

    @Column(name = "iznos_popusta", nullable = false, precision = 12, scale = 4)
    private BigDecimal iznosPopusta = BigDecimal.ZERO;

    @Column(name = "iznos_pdv", nullable = false, precision = 12, scale = 4)
    private BigDecimal iznosPdv = BigDecimal.ZERO;
}
