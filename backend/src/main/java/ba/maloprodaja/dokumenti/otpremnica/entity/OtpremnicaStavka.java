package ba.maloprodaja.dokumenti.otpremnica.entity;

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
@Table(name = "otpremnice_stavke")
public class OtpremnicaStavka extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_otpremnice", nullable = false)
    private Otpremnica otpremnica;

    @Column(name = "id_artikla", nullable = false)
    private Long idArtikla;

    @Column(name = "kolicina", nullable = false, precision = 15, scale = 4)
    private BigDecimal kolicina;

    @Column(name = "vpc_posiljalac", nullable = false, precision = 15, scale = 4)
    private BigDecimal vpcPosiljalac;

    @Column(name = "mpc_posiljalac", nullable = false, precision = 15, scale = 4)
    private BigDecimal mpcPosiljalac;
}
