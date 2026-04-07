package ba.maloprodaja.dokumenti.nivelacija.entity;

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
@Table(name = "nivelacije_stavke")
public class NivelacijaStavka extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nivelacije", nullable = false)
    private Nivelacija nivelacija;

    @Column(name = "id_artikla", nullable = false)
    private Long idArtikla;

    @Column(name = "kolicina", nullable = false, precision = 15, scale = 4)
    private BigDecimal kolicina;

    @Column(name = "vpc_stara", nullable = false, precision = 15, scale = 4)
    private BigDecimal vpcStara;

    @Column(name = "vpc_nova", nullable = false, precision = 15, scale = 4)
    private BigDecimal vpcNova;

    @Column(name = "mpc_stara", nullable = false, precision = 15, scale = 4)
    private BigDecimal mpcStara;

    @Column(name = "mpc_nova", nullable = false, precision = 15, scale = 4)
    private BigDecimal mpcNova;

    @Column(name = "iznos_nivelacije", nullable = false, precision = 15, scale = 4)
    private BigDecimal iznosNivelacije;
}
