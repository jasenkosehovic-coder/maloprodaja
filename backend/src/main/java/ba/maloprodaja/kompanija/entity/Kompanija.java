package ba.maloprodaja.kompanija.entity;

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
 * Tenant root entity. Does NOT extend BaseEntity because BaseEntity requires
 * id_kompanije NOT NULL, which would be a self-referencing circular constraint.
 * All audit fields are defined directly here.
 */
@Getter
@Setter
@Entity
@Table(name = "kompanije")
@EntityListeners(AuditingEntityListener.class)
public class Kompanija {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "naziv", nullable = false, length = 200)
    private String naziv;

    @Column(name = "pib", unique = true, length = 50)
    private String pib;

    @Column(name = "adresa", length = 300)
    private String adresa;

    @Column(name = "grad", length = 100)
    private String grad;

    @Column(name = "telefon", length = 30)
    private String telefon;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "web", length = 200)
    private String web;

    @Column(name = "logo", columnDefinition = "BYTEA")
    private byte[] logo;

    @Column(name = "aktivan", nullable = false)
    private boolean aktivan = true;

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
