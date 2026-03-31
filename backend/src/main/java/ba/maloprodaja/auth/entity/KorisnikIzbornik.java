package ba.maloprodaja.auth.entity;

import ba.maloprodaja.common.entity.KompanijaBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "korisnik_izbornici",
        uniqueConstraints = @UniqueConstraint(columnNames = {"korisnik_id", "izbornik_kljuc"}))
public class KorisnikIzbornik extends KompanijaBaseEntity {

    @Column(name = "korisnik_id", nullable = false)
    private Long korisnikId;

    @Column(name = "izbornik_kljuc", nullable = false, length = 50)
    private String izbornikKljuc;

    @Column(name = "aktivan", nullable = false)
    private boolean aktivan = true;
}
