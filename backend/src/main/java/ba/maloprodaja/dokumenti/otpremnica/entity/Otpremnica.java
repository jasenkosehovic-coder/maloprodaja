package ba.maloprodaja.dokumenti.otpremnica.entity;

import ba.maloprodaja.common.entity.KompanijaBaseEntity;
import ba.maloprodaja.dokumenti.enums.StatusOtpremnice;
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
        name = "otpremnice",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_otpremnica_kompanija_broj",
                columnNames = {"id_kompanije", "broj"}
        )
)
public class Otpremnica extends KompanijaBaseEntity {

    @Column(name = "id_poslovnice_posiljaoca", nullable = false)
    private Long idPoslovnicePosiljaoca;

    @Column(name = "id_poslovnice_primaoca", nullable = false)
    private Long idPoslovnicePrimaoca;

    @Column(name = "broj", nullable = false, length = 50)
    private String broj;

    @Column(name = "datum", nullable = false)
    private LocalDate datum;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusOtpremnice status = StatusOtpremnice.KREIRANA;

    @Column(name = "napomena", length = 500)
    private String napomena;

    @OneToMany(mappedBy = "otpremnica", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OtpremnicaStavka> stavke = new ArrayList<>();
}
