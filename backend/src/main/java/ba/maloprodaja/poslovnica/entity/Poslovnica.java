package ba.maloprodaja.poslovnica.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Branch (poslovnica) entity. Does NOT extend BaseEntity because a branch has
 * only id_kompanije (not id_poslovnice), so the BaseEntity contract does not apply.
 * All audit fields are defined directly here.
 */
@Getter
@Setter
@Entity
@Table(name = "poslovnice")
@EntityListeners(AuditingEntityListener.class)
public class Poslovnica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "naziv", nullable = false, length = 200)
    private String naziv;

    @Column(name = "adresa", length = 300)
    private String adresa;

    @Column(name = "grad", length = 100)
    private String grad;

    @Column(name = "telefon", length = 30)
    private String telefon;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "aktivan", nullable = false)
    private boolean aktivan = true;

    @Column(name = "id_kompanije", nullable = false)
    private Long idKompanije;

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
}
