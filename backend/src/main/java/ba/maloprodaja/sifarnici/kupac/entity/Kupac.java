package ba.maloprodaja.sifarnici.kupac.entity;

import ba.maloprodaja.common.entity.KompanijaBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "kupci")
public class Kupac extends KompanijaBaseEntity {

    @Column(name = "naziv", nullable = false, length = 200)
    private String naziv;

    @Column(name = "adresa", length = 300)
    private String adresa;

    @Column(name = "grad", length = 100)
    private String grad;

    @Column(name = "telefon", length = 50)
    private String telefon;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "pib", length = 50)
    private String pib;

    @Column(name = "aktivan", nullable = false)
    private boolean aktivan = true;
}
