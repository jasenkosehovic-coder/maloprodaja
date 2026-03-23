package ba.maloprodaja.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "sys_created_date", nullable = false, updatable = false)
    private LocalDateTime sysCreatedDate;

    @LastModifiedDate
    @Column(name = "sys_modified_date")
    private LocalDateTime sysModifiedDate;

    @CreatedBy
    @Column(name = "sys_created_by", updatable = false)
    private Long sysCreatedBy;

    @LastModifiedBy
    @Column(name = "sys_modified_by")
    private Long sysModifiedBy;

    @Column(name = "id_kompanije", nullable = false)
    private Long idKompanije;

    @Column(name = "id_poslovnice")
    private Long idPoslovnice;
}
