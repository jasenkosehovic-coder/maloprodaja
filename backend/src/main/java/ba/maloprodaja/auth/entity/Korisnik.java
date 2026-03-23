package ba.maloprodaja.auth.entity;

import ba.maloprodaja.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "korisnici")
public class Korisnik extends BaseEntity implements UserDetails {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "uloga", nullable = false, length = 20)
    private KorisnikUloga uloga;

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
