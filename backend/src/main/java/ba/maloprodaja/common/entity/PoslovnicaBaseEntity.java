package ba.maloprodaja.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class PoslovnicaBaseEntity extends KompanijaBaseEntity {

    @Column(name = "id_poslovnice", nullable = false)
    private Long idPoslovnice;
}
