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

    @Column(name = "cijena", nullable = false, precision = 12, scale = 4)
    private BigDecimal cijena;

    @Column(name = "popust", nullable = false, precision = 5, scale = 2)
    private BigDecimal popust = BigDecimal.ZERO;

    @Column(name = "ukupno", nullable = false, precision = 12, scale = 4)
    private BigDecimal ukupno;
}
