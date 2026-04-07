package ba.maloprodaja.promet.brojac.entity;

import ba.maloprodaja.promet.tipdokumenta.entity.TipDokumenta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "brojaci_dokumenata",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_brojac_tip_kompanija_godina",
                columnNames = {"id_tipa", "id_kompanije", "godina"}
        )
)
public class BrojacDokumenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipa", nullable = false)
    private TipDokumenta tipDokumenta;

    @Column(name = "id_kompanije", nullable = false)
    private Long idKompanije;

    @Column(name = "godina", nullable = false)
    private Short godina;

    @Column(name = "brojac", nullable = false)
    private int brojac = 0;
}
