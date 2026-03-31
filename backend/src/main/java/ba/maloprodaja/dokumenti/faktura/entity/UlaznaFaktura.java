package ba.maloprodaja.dokumenti.faktura.entity;

import ba.maloprodaja.common.entity.PoslovnicaBaseEntity;
import ba.maloprodaja.dokumenti.enums.StatusFakture;
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
@Table(
        name = "ulazne_fakture",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_faktura_kompanija_poslovnica_broj",
                columnNames = {"id_kompanije", "id_poslovnice", "broj"}
        )
)
public class UlaznaFaktura extends PoslovnicaBaseEntity {

    @Column(name = "id_dobavljaca", nullable = false)
    private Long idDobavljaca;

    @Column(name = "broj", nullable = false, length = 50)
    private String broj;

    @Column(name = "datum", nullable = false)
    private LocalDate datum;

    @Column(name = "datum_valute")
    private LocalDate datumValute;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusFakture status = StatusFakture.NACRT;

    @Column(name = "ukupno_bez_pdv", nullable = false, precision = 15, scale = 4)
    private BigDecimal ukupnoBezPdv = BigDecimal.ZERO;

    @Column(name = "ukupno_pdv", nullable = false, precision = 15, scale = 4)
    private BigDecimal ukupnoPdv = BigDecimal.ZERO;

    @Column(name = "ukupno", nullable = false, precision = 15, scale = 4)
    private BigDecimal ukupno = BigDecimal.ZERO;

    @Column(name = "napomena", length = 500)
    private String napomena;

    @OneToMany(mappedBy = "faktura", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UlaznaFakturaStavka> stavke = new ArrayList<>();
}
