package ba.maloprodaja.auth.entity;

import ba.maloprodaja.common.entity.KompanijaBaseEntity;
import ba.maloprodaja.kompanija.entity.Kompanija;
import ba.maloprodaja.poslovnica.entity.Poslovnica;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "korisnici")
public class Korisnik extends KompanijaBaseEntity implements UserDetails {

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "ime", length = 100)
    private String ime;

    @Column(name = "prezime", length = 100)
    private String prezime;

    @Column(name = "email", unique = true, length = 150)
    private String email;

    @Column(name = "aktivan", nullable = false)
    private boolean aktivan = true;

    @Column(name = "aktivan_do")
    private LocalDate aktivDo;

    @Enumerated(EnumType.STRING)
    @Column(name = "uloga", nullable = false, columnDefinition = "varchar(20)")
    private KorisnikUloga uloga;

    @Column(name = "id_poslovnice")
    private Long idPoslovnice;

    // ---- Read-only navigation (writes go through idKompanije / idPoslovnice) ----

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_kompanije", insertable = false, updatable = false)
    private Kompanija kompanija;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_poslovnice", insertable = false, updatable = false)
    private Poslovnica poslovnica;

    // ---- UserDetails ----

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + uloga.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return aktivan;
    }
}
