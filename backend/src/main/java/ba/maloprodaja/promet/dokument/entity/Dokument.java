package ba.maloprodaja.promet.dokument.entity;

import ba.maloprodaja.common.entity.KompanijaBaseEntity;
import ba.maloprodaja.promet.stavka.entity.StavkaDokumenta;
import ba.maloprodaja.promet.tipdokumenta.entity.TipDokumenta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "dokumenti")
public class Dokument extends KompanijaBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipa", nullable = false)
    private TipDokumenta tipDokumenta;

    @Column(name = "id_poslovnice", nullable = false)
    private Long idPoslovnice;

    @Column(name = "id_dobavljaca")
    private Long idDobavljaca;

    @Column(name = "id_kupca")
    private Long idKupca;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusDokumenta status = StatusDokumenta.NACRT;

    @Column(name = "broj_dokumenta", length = 30)
    private String brojDokumenta;

    @Column(name = "datum", nullable = false)
    private LocalDate datum;

    @Column(name = "napomena", columnDefinition = "TEXT")
    private String napomena;

    @Column(name = "iznos_mpc", nullable = false, precision = 14, scale = 4)
    private BigDecimal iznosMpc = BigDecimal.ZERO;

    @Column(name = "iznos_vpc", nullable = false, precision = 14, scale = 4)
    private BigDecimal iznosVpc = BigDecimal.ZERO;

    @Column(name = "iznos_pdv", nullable = false, precision = 14, scale = 4)
    private BigDecimal iznosPdv = BigDecimal.ZERO;

    @Column(name = "iznos_popusta", nullable = false, precision = 14, scale = 4)
    private BigDecimal iznosPopusta = BigDecimal.ZERO;

    @Column(name = "iznos_marze", nullable = false, precision = 14, scale = 4)
    private BigDecimal iznosMarze = BigDecimal.ZERO;

    @OneToMany(mappedBy = "dokument", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<StavkaDokumenta> stavke = new ArrayList<>();
}
