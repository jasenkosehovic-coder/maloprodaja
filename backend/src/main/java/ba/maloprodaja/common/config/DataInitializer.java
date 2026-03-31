package ba.maloprodaja.common.config;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.auth.entity.KorisnikIzbornik;
import ba.maloprodaja.auth.entity.KorisnikUloga;
import ba.maloprodaja.auth.repository.KorisnikIzborniciRepository;
import ba.maloprodaja.auth.repository.KorisnikRepository;
import ba.maloprodaja.kompanija.entity.Kompanija;
import ba.maloprodaja.kompanija.repository.KompanijaRepository;
import ba.maloprodaja.poslovnica.entity.Poslovnica;
import ba.maloprodaja.poslovnica.repository.PoslovnicaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.lang.Nullable;

import java.util.List;

@Slf4j
@Component
@Order(1)
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

    // Seed users: username, password, ime, prezime, uloga, kompanija PIB, poslovnica naziv (null = no poslovnica)
    private static final List<SeedUser> SEED_USERS = List.of(
            new SeedUser("admin",       "Admin123!", "Admin", "Korisnik", KorisnikUloga.ADMIN,       "1234567890", "Centrala"),
            new SeedUser("admin1",      "Admin123!", "Admin", "Jedan",    KorisnikUloga.ADMIN,       "1234567890", "Centrala"),
            new SeedUser("admin2",      "Admin123!", "Admin", "Dva",      KorisnikUloga.ADMIN,       "1234567890", "Poslovnica 2"),
            new SeedUser("admin3",      "Admin123!", "Admin", "Tri",      KorisnikUloga.ADMIN,       "0987654321", "Centrala"),
            new SeedUser("admin4",      "Admin123!", "Admin", "Cetiri",   KorisnikUloga.ADMIN,       "0987654321", "Poslovnica 2"),
            new SeedUser("superadmin1", "Admin123!", "Super", "Admin1",   KorisnikUloga.SUPER_ADMIN, "1234567890", null),
            new SeedUser("superadmin2", "Admin123!", "Super", "Admin2",   KorisnikUloga.SUPER_ADMIN, "0987654321", null)
    );

    private final KorisnikRepository korisnikRepository;
    private final KorisnikIzborniciRepository korisnikIzborniciRepository;
    private final KompanijaRepository kompanijaRepository;
    private final PoslovnicaRepository poslovnicaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedKompanije();
        seedUsers();
    }

    @Transactional
    protected void seedKompanije() {
        if (kompanijaRepository.count() > 0) {
            return;
        }
        log.info("Kompanije nisu pronađene u bazi — upisujem seed podatke.");

        Kompanija k1 = new Kompanija();
        k1.setNaziv("Trgovina d.o.o.");
        k1.setPib("1234567890");
        k1.setAdresa("neka adresa");
        k1.setEmail("trg@boom.ba");
        k1.setGrad("Sarajevo");
        k1.setTelefon("1230123");
        k1.setWeb("www.trgovina.ba");
        k1.setAktivan(true);
        Kompanija saved1 = kompanijaRepository.save(k1);

        Kompanija k2 = new Kompanija();
        k2.setNaziv("Prodavnica d.o.o.");
        k2.setPib("0987654321");
        k2.setAdresa("neka adresa 2");
        k2.setEmail("trg2@boom.ba");
        k2.setGrad("Mostar");
        k2.setTelefon("7894789");
        k2.setWeb("www.prodavnica.ba");
        k2.setAktivan(true);
        Kompanija saved2 = kompanijaRepository.save(k2);

        seedPoslovnice(saved1, saved2);
    }

    private void seedPoslovnice(Kompanija k1, Kompanija k2) {
        savePoslovnica("Centrala",     "Titova 1",     k1.getId(), "Sarajevo", "info1@boom.ba", "123654");
        savePoslovnica("Poslovnica 2", "Ferhadija 10", k1.getId(), "Sarajevo", "info2@boom.ba", "1236545");
        savePoslovnica("Centrala",     "Bulevar 5",      k2.getId(), "Mostar", "info3@boom.ba", "1236546");
        savePoslovnica("Poslovnica 2", "Rondo 3",        k2.getId(), "Mostar", "info4@boom.ba", "1236547");
        log.info("Kreirane 4 poslovnice.");
    }

    private void savePoslovnica(String naziv, String adresa, Long idKompanije, String grad, String mail, String tel) {
        Poslovnica p = new Poslovnica();
        p.setNaziv(naziv);
        p.setAdresa(adresa);
        p.setAktivan(true);
        p.setIdKompanije(idKompanije);
        p.setGrad(grad);
        p.setEmail(mail);
        p.setTelefon(tel);
        poslovnicaRepository.save(p);
    }

    @Transactional
    protected void seedUsers() {
        for (SeedUser seed : SEED_USERS) {
            if (korisnikRepository.findByUsername(seed.username()).isPresent()) {
                ensureIzbornici(seed.username());
                continue;
            }

            Long idKompanije = kompanijaRepository.findByPib(seed.kompanijaPib())
                    .orElseThrow(() -> new IllegalStateException(
                            "Kompanija s PIB-om '%s' nije pronađena u bazi.".formatted(seed.kompanijaPib())))
                    .getId();

            Long idPoslovnice = null;
            if (seed.poslovnicaNaziv() != null) {
                idPoslovnice = poslovnicaRepository.findByNazivAndIdKompanije(seed.poslovnicaNaziv(), idKompanije)
                        .orElseThrow(() -> new IllegalStateException(
                                "Poslovnica '%s' nije pronađena za kompaniju %d.".formatted(seed.poslovnicaNaziv(), idKompanije)))
                        .getId();
            }

            Korisnik korisnik = new Korisnik();
            korisnik.setUsername(seed.username());
            korisnik.setPasswordHash(passwordEncoder.encode(seed.password()));
            korisnik.setIme(seed.ime());
            korisnik.setPrezime(seed.prezime());
            korisnik.setEmail(seed.username() + "@maloprodaja.ba");
            korisnik.setAktivan(true);
            korisnik.setUloga(seed.uloga());
            korisnik.setIdKompanije(idKompanije);
            korisnik.setIdPoslovnice(idPoslovnice);

            Korisnik saved = korisnikRepository.save(korisnik);
            log.info("Kreiran korisnik: username='{}', id={}, idKompanije={}, idPoslovnice={}",
                    saved.getUsername(), saved.getId(), idKompanije, idPoslovnice);

            seedIzbornici(saved);
        }
    }

    private void ensureIzbornici(String username) {
        korisnikRepository.findByUsername(username).ifPresent(korisnik -> {
            long existingCount = korisnikIzborniciRepository.countByKorisnikId(korisnik.getId());
            if (existingCount == 0) {
                seedIzbornici(korisnik);
            }
        });
    }

    private void seedIzbornici(Korisnik korisnik) {
        List<KorisnikIzbornik> izbornici = ADMIN_IZBORNICI.stream().map(kljuc -> {
            KorisnikIzbornik i = new KorisnikIzbornik();
            i.setKorisnikId(korisnik.getId());
            i.setIzbornikKljuc(kljuc);
            i.setAktivan(true);
            i.setIdKompanije(korisnik.getIdKompanije());
            return i;
        }).toList();

        korisnikIzborniciRepository.saveAll(izbornici);
        log.info("Dodano {} izbornika za korisnika '{}'.", izbornici.size(), korisnik.getUsername());
    }

    private record SeedUser(
            String username,
            String password,
            String ime,
            String prezime,
            KorisnikUloga uloga,
            String kompanijaPib,
            @Nullable String poslovnicaNaziv
    ) {}
}
