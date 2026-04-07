package ba.maloprodaja.sifarnici.boja.entity;

import ba.maloprodaja.common.entity.KompanijaBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "boje")
public class Boja extends KompanijaBaseEntity {

    @Column(name = "naziv", nullable = false, length = 100)
    private String naziv;

    @Column(name = "hex_kod", length = 7)
    private String hexKod;

    @Column(name = "aktivan", nullable = false)
    private boolean aktivan = true;
}
