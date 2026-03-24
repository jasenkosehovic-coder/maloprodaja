package ba.maloprodaja.common.config;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.auth.entity.KorisnikIzbornik;
import ba.maloprodaja.auth.entity.KorisnikUloga;
import ba.maloprodaja.auth.repository.KorisnikIzborniciRepository;
import ba.maloprodaja.auth.repository.KorisnikRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final List<String> ADMIN_IZBORNICI = List.of(
            "sifarnici.artikli",
            "sifarnici.grupe-artikala",
            "sifarnici.artikli-poslovnica",
            "sifarnici.barkodovi",
            "sifarnici.proizvodjaci",
            "sifarnici.dobavljaci",
            "sifarnici.kupci",
            "sifarnici.korisnici",
            "blagajna",
            "blagajna.izvjestaji",
            "fiskalni.printer",
            "fiskalni.dnevni",
            "fiskalni.presijek",
            "fiskalni.periodicni",
            "fiskalni.test",
            "dokumenti.ulazne-fakture",
            "dokumenti.otpremnica",
            "dokumenti.nivelacije",
            "izvjestaji.knjiga-blagajni",
            "izvjestaji.trgovacka-knjiga",
            "izvjestaji.stanje-zaliha",
            "izvjestaji.lager-lista",
            "izvjestaji.dokumenti",
            "postavke.parametri",
            "chat"
    );

    private final KorisnikRepository korisnikRepository;
    private final KorisnikIzborniciRepository korisnikIzborniciRepository;
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

            Korisnik saved = korisnikRepository.save(admin);
            log.info("Admin korisnik kreiran: username='admin', id={}", saved.getId());

            List<KorisnikIzbornik> izbornici = ADMIN_IZBORNICI.stream().map(kljuc -> {
                KorisnikIzbornik i = new KorisnikIzbornik();
                i.setKorisnikId(saved.getId());
                i.setIzbornikKljuc(kljuc);
                i.setAktivan(true);
                i.setIdKompanije(saved.getIdKompanije());
                i.setIdPoslovnice(saved.getIdPoslovnice());
                return i;
            }).toList();

            korisnikIzborniciRepository.saveAll(izbornici);
            log.info("Dodano {} izbornika za admin korisnika.", izbornici.size());
        }
    }
}
