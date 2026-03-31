package ba.maloprodaja.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class KompanijaBaseEntity extends BaseEntity {

    @Column(name = "id_kompanije", nullable = false)
    private Long idKompanije;
}
