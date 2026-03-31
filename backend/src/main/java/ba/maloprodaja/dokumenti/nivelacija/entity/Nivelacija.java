package ba.maloprodaja.dokumenti.nivelacija.entity;

import ba.maloprodaja.common.entity.PoslovnicaBaseEntity;
import ba.maloprodaja.dokumenti.enums.VrstaNivelacije;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "nivelacije",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_nivelacija_kompanija_poslovnica_broj",
                columnNames = {"id_kompanije", "id_poslovnice", "broj"}
        )
)
public class Nivelacija extends PoslovnicaBaseEntity {

    @Column(name = "id_fakture")
    private Long idFakture;

    @Column(name = "id_otpremnice")
    private Long idOtpremnice;

    @Column(name = "broj", nullable = false, length = 50)
    private String broj;

    @Column(name = "datum", nullable = false)
    private LocalDate datum;

    @Enumerated(EnumType.STRING)
    @Column(name = "vrsta", nullable = false, length = 30)
    private VrstaNivelacije vrsta;

    @Column(name = "napomena", length = 500)
    private String napomena;

    @OneToMany(mappedBy = "nivelacija", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<NivelacijaStavka> stavke = new ArrayList<>();
}
