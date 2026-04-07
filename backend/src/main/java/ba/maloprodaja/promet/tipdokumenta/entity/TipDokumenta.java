package ba.maloprodaja.promet.tipdokumenta.entity;

import ba.maloprodaja.common.entity.KompanijaBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "tipovi_dokumenata",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_tip_dokumenta_kod_kompanija",
                columnNames = {"kod", "id_kompanije"}
        )
)
public class TipDokumenta extends KompanijaBaseEntity {

    @Column(name = "kod", nullable = false, length = 20)
    private String kod;

    @Column(name = "naziv", nullable = false, length = 100)
    private String naziv;

    /**
     * +1 for inbound (ulaz), -1 for outbound (izlaz).
     * Applied to stavka.kolicina when updating stock on potvrdi.
     */
    @Column(name = "smjer_kolicine", nullable = false)
    private Short smjerKolicine;
}
