package ba.maloprodaja.sifarnici.proizvodjac.entity;

import ba.maloprodaja.common.entity.KompanijaBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "proizvodjaci")
public class Proizvodjac extends KompanijaBaseEntity {

    @Column(name = "naziv", nullable = false, length = 200)
    private String naziv;

    @Column(name = "drzava", length = 100)
    private String drzava;

    @Column(name = "kontakt_osoba", length = 150)
    private String kontaktOsoba;

    @Column(name = "telefon", length = 50)
    private String telefon;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "aktivan", nullable = false)
    private boolean aktivan = true;
}
