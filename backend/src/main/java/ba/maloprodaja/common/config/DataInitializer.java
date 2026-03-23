package ba.maloprodaja.common.config;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.auth.entity.KorisnikUloga;
import ba.maloprodaja.auth.repository.KorisnikRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final KorisnikRepository korisnikRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (korisnikRepository.count() == 0) {
            log.info("Nema korisnika u bazi — upisujem podrazumijevanog admin korisnika.");

            Korisnik admin = new Korisnik();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("Admin123!"));
            admin.setIme("Admin");
            admin.setPrezime("Korisnik");
            admin.setEmail("admin@maloprodaja.ba");
            admin.setAktivan(true);
            admin.setUloga(KorisnikUloga.ADMIN);
            admin.setIdKompanije(1L);

            korisnikRepository.save(admin);
            log.info("Admin korisnik kreiran: username='admin', lozinka='Admin123!'");
        }
    }
}
