package ba.maloprodaja.dokumenti.faktura.entity;

import ba.maloprodaja.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ulazne_fakture_stavke")
public class UlaznaFakturaStavka extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fakture", nullable = false)
    private UlaznaFaktura faktura;

    @Column(name = "id_artikla", nullable = false)
    private Long idArtikla;

    @Column(name = "kolicina", nullable = false, precision = 15, scale = 4)
    private BigDecimal kolicina;

    @Column(name = "vpc", nullable = false, precision = 15, scale = 4)
    private BigDecimal vpc;

    @Column(name = "pdv_stopa", nullable = false, precision = 10, scale = 4)
    private BigDecimal pdvStopa;

    @Column(name = "iznos_pdv", nullable = false, precision = 15, scale = 4)
    private BigDecimal iznosPdv = BigDecimal.ZERO;

    @Column(name = "ukupno", nullable = false, precision = 15, scale = 4)
    private BigDecimal ukupno = BigDecimal.ZERO;
}
