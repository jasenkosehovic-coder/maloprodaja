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
import ba.maloprodaja.sifarnici.artikalkomp.entity.ArtikalKompanija;
import ba.maloprodaja.sifarnici.artikalkomp.entity.TipMarze;
import ba.maloprodaja.sifarnici.artikalkomp.repository.ArtikalKompanijeRepository;
import ba.maloprodaja.sifarnici.artikalposl.entity.ArtikalPoslovnica;
import ba.maloprodaja.sifarnici.artikalposl.repository.ArtikalPoslovnicaRepository;
import ba.maloprodaja.sifarnici.atribut.entity.DefinicijaAtributa;
import ba.maloprodaja.sifarnici.atribut.entity.VrijednostAtributa;
import ba.maloprodaja.sifarnici.atribut.repository.DefinicijaAtributaRepository;
import ba.maloprodaja.sifarnici.atribut.repository.VrijednostAtributaRepository;
import ba.maloprodaja.sifarnici.boja.entity.Boja;
import ba.maloprodaja.sifarnici.boja.repository.BojaRepository;
import ba.maloprodaja.sifarnici.dobavljac.entity.Dobavljac;
import ba.maloprodaja.sifarnici.dobavljac.repository.DobavljacRepository;
import ba.maloprodaja.sifarnici.grupaartikala.entity.GrupaArtikala;
import ba.maloprodaja.sifarnici.grupaartikala.repository.GrupaArtikalaRepository;
import ba.maloprodaja.sifarnici.kupac.entity.Kupac;
import ba.maloprodaja.sifarnici.kupac.repository.KupacRepository;
import ba.maloprodaja.sifarnici.proizvodjac.entity.Proizvodjac;
import ba.maloprodaja.sifarnici.proizvodjac.repository.ProizvodjacRepository;
import ba.maloprodaja.promet.tipdokumenta.entity.TipDokumenta;
import ba.maloprodaja.promet.tipdokumenta.repository.TipDokumentaRepository;
import ba.maloprodaja.sifarnici.varijanta.entity.VarijantaArtikla;
import ba.maloprodaja.sifarnici.varijanta.entity.VarijantaArtiklaPoslovnica;
import ba.maloprodaja.sifarnici.varijanta.repository.VarijantaArtiklaRepository;
import ba.maloprodaja.sifarnici.varijanta.repository.VarijantaArtiklaPoslovnicaRepository;
import ba.maloprodaja.sifarnici.velicina.entity.TipVelicina;
import ba.maloprodaja.sifarnici.velicina.entity.Velicina;
import ba.maloprodaja.sifarnici.velicina.repository.TipVelicinaRepository;
import ba.maloprodaja.sifarnici.velicina.repository.VelicinaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.lang.Nullable;

import java.math.BigDecimal;
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
            "sifarnici.tipovi-velicina",
            "sifarnici.definicije-atributa",
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
    private final ProizvodjacRepository proizvodjacRepository;
    private final DobavljacRepository dobavljacRepository;
    private final KupacRepository kupacRepository;
    private final GrupaArtikalaRepository grupaArtikalaRepository;
    private final ArtikalKompanijeRepository artikalKompanijeRepository;
    private final ArtikalPoslovnicaRepository artikalPoslovnicaRepository;
    private final VarijantaArtiklaRepository varijantaArtiklaRepository;
    private final VarijantaArtiklaPoslovnicaRepository varijantaArtiklaPoslovnicaRepository;
    private final TipVelicinaRepository tipVelicinaRepository;
    private final VelicinaRepository velicinaRepository;
    private final BojaRepository bojaRepository;
    private final DefinicijaAtributaRepository definicijaAtributaRepository;
    private final VrijednostAtributaRepository vrijednostAtributaRepository;
    private final TipDokumentaRepository tipDokumentaRepository;

    @Override
    public void run(ApplicationArguments args) {
        seedKompanije();
        seedUsers();
        seedProizvodjaci();
        seedDobavljaci();
        seedKupci();
        seedGrupeArtikala();
        seedTipoviVelicina();
        seedArtikliKompanije();
        seedArtikliPoslovnice();
        seedBoje();
        seedDefinicijeAtributa();
        seedTipoviDokumenata();
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

    @Transactional
    protected void seedProizvodjaci() {
        if (proizvodjacRepository.count() > 0) {
            return;
        }

        Long idKompanije = kompanijaRepository.findByPib("1234567890")
                .orElseThrow(() -> new IllegalStateException("Kompanija s PIB-om '1234567890' nije pronađena."))
                .getId();

        Proizvodjac p1 = new Proizvodjac();
        p1.setIdKompanije(idKompanije);
        p1.setNaziv("Zara (Inditex)");
        p1.setDrzava("Španija");
        p1.setKontaktOsoba("María García");
        p1.setTelefon("+34 91 123 4567");
        p1.setEmail("sourcing@zara.com");
        p1.setAktivan(true);
        proizvodjacRepository.save(p1);
        log.info("Kreiran proizvođač: Zara (Inditex)");

        Proizvodjac p2 = new Proizvodjac();
        p2.setIdKompanije(idKompanije);
        p2.setNaziv("H&M Group");
        p2.setDrzava("Švedska");
        p2.setKontaktOsoba("Lars Eriksson");
        p2.setTelefon("+46 8 796 5500");
        p2.setEmail("supply@hm.com");
        p2.setAktivan(true);
        proizvodjacRepository.save(p2);
        log.info("Kreiran proizvođač: H&M Group");

        Proizvodjac p3 = new Proizvodjac();
        p3.setIdKompanije(idKompanije);
        p3.setNaziv("Mango");
        p3.setDrzava("Španija");
        p3.setKontaktOsoba("Carlos López");
        p3.setTelefon("+34 93 860 9000");
        p3.setEmail("orders@mango.com");
        p3.setAktivan(true);
        proizvodjacRepository.save(p3);
        log.info("Kreiran proizvođač: Mango");

        Proizvodjac p4 = new Proizvodjac();
        p4.setIdKompanije(idKompanije);
        p4.setNaziv("Nike Inc.");
        p4.setDrzava("SAD");
        p4.setKontaktOsoba("James Wilson");
        p4.setTelefon("+1 503 671 6453");
        p4.setEmail("wholesale@nike.com");
        p4.setAktivan(true);
        proizvodjacRepository.save(p4);
        log.info("Kreiran proizvođač: Nike Inc.");

        Proizvodjac p5 = new Proizvodjac();
        p5.setIdKompanije(idKompanije);
        p5.setNaziv("Adidas AG");
        p5.setDrzava("Njemačka");
        p5.setKontaktOsoba("Hans Müller");
        p5.setTelefon("+49 9132 84 0");
        p5.setEmail("orders@adidas.com");
        p5.setAktivan(true);
        proizvodjacRepository.save(p5);
        log.info("Kreiran proizvođač: Adidas AG");

        Proizvodjac p6 = new Proizvodjac();
        p6.setIdKompanije(idKompanije);
        p6.setNaziv("Levi Strauss & Co.");
        p6.setDrzava("SAD");
        p6.setKontaktOsoba("Sarah Johnson");
        p6.setTelefon("+1 415 501 6000");
        p6.setEmail("trade@levis.com");
        p6.setAktivan(true);
        proizvodjacRepository.save(p6);
        log.info("Kreiran proizvođač: Levi Strauss & Co.");
    }

    @Transactional
    protected void seedDobavljaci() {
        if (dobavljacRepository.count() > 0) {
            return;
        }

        Long idKompanije = kompanijaRepository.findByPib("1234567890")
                .orElseThrow(() -> new IllegalStateException("Kompanija s PIB-om '1234567890' nije pronađena."))
                .getId();

        Dobavljac d1 = new Dobavljac();
        d1.setIdKompanije(idKompanije);
        d1.setNaziv("Tekstil Import d.o.o.");
        d1.setAdresa("Maršala Tita 22");
        d1.setGrad("Sarajevo");
        d1.setTelefon("033 201 301");
        d1.setEmail("nabavka@tekstilimport.ba");
        d1.setPib("1111111111");
        d1.setAktivan(true);
        dobavljacRepository.save(d1);
        log.info("Kreiran dobavljač: Tekstil Import d.o.o.");

        Dobavljac d2 = new Dobavljac();
        d2.setIdKompanije(idKompanije);
        d2.setNaziv("Fashion Trade d.o.o.");
        d2.setAdresa("Ilica 55");
        d2.setGrad("Zagreb");
        d2.setTelefon("+385 1 234 5678");
        d2.setEmail("trade@fashiontrade.hr");
        d2.setPib("2222222222");
        d2.setAktivan(true);
        dobavljacRepository.save(d2);
        log.info("Kreiran dobavljač: Fashion Trade d.o.o.");

        Dobavljac d3 = new Dobavljac();
        d3.setIdKompanije(idKompanije);
        d3.setNaziv("Euro Tekstil d.o.o.");
        d3.setAdresa("Kralja Petra I 8");
        d3.setGrad("Banja Luka");
        d3.setTelefon("051 302 403");
        d3.setEmail("info@eurotekstil.ba");
        d3.setPib("3333333333");
        d3.setAktivan(true);
        dobavljacRepository.save(d3);
        log.info("Kreiran dobavljač: Euro Tekstil d.o.o.");

        Dobavljac d4 = new Dobavljac();
        d4.setIdKompanije(idKompanije);
        d4.setNaziv("Global Fashion d.o.o.");
        d4.setAdresa("Braće Fejića 12");
        d4.setGrad("Mostar");
        d4.setTelefon("036 401 502");
        d4.setEmail("orders@globalfashion.ba");
        d4.setPib("4444444444");
        d4.setAktivan(true);
        dobavljacRepository.save(d4);
        log.info("Kreiran dobavljač: Global Fashion d.o.o.");
    }

    @Transactional
    protected void seedKupci() {
        if (kupacRepository.count() > 0) {
            return;
        }

        Long idKompanije = kompanijaRepository.findByPib("1234567890")
                .orElseThrow(() -> new IllegalStateException("Kompanija s PIB-om '1234567890' nije pronađena."))
                .getId();

        Kupac k1 = new Kupac();
        k1.setIdKompanije(idKompanije);
        k1.setNaziv("Firma ABC d.o.o.");
        k1.setAdresa("Ferhadija 22");
        k1.setGrad("Sarajevo");
        k1.setTelefon("033 111 222");
        k1.setEmail("nabavka@firmaABC.ba");
        k1.setPib("5555555555");
        k1.setAktivan(true);
        kupacRepository.save(k1);
        log.info("Kreiran kupac: Firma ABC d.o.o.");

        Kupac k2 = new Kupac();
        k2.setIdKompanije(idKompanije);
        k2.setNaziv("Kompanija XYZ d.o.o.");
        k2.setAdresa("Maršala Tita 10");
        k2.setGrad("Zenica");
        k2.setTelefon("032 333 444");
        k2.setEmail("info@kompanija-xyz.ba");
        k2.setPib("6666666666");
        k2.setAktivan(true);
        kupacRepository.save(k2);
        log.info("Kreiran kupac: Kompanija XYZ d.o.o.");

        Kupac k3 = new Kupac();
        k3.setIdKompanije(idKompanije);
        k3.setNaziv("Prom-Ex d.o.o.");
        k3.setAdresa("Bulevar 15");
        k3.setGrad("Tuzla");
        k3.setTelefon("035 555 666");
        k3.setEmail("nabavka@promex.ba");
        k3.setPib("7777777777");
        k3.setAktivan(true);
        kupacRepository.save(k3);
        log.info("Kreiran kupac: Prom-Ex d.o.o.");

        Kupac k4 = new Kupac();
        k4.setIdKompanije(idKompanije);
        k4.setNaziv("Retail Plus d.o.o.");
        k4.setAdresa("Kralja Tomislava 3");
        k4.setGrad("Mostar");
        k4.setTelefon("036 777 888");
        k4.setEmail("info@retailplus.ba");
        k4.setPib("8888888888");
        k4.setAktivan(true);
        kupacRepository.save(k4);
        log.info("Kreiran kupac: Retail Plus d.o.o.");

        Kupac k5 = new Kupac();
        k5.setIdKompanije(idKompanije);
        k5.setNaziv("TechBuy d.o.o.");
        k5.setAdresa("Aleja Lipa 7");
        k5.setGrad("Banja Luka");
        k5.setTelefon("051 999 000");
        k5.setEmail("kupovina@techbuy.ba");
        k5.setPib("9999999999");
        k5.setAktivan(true);
        kupacRepository.save(k5);
        log.info("Kreiran kupac: TechBuy d.o.o.");
    }

    @Transactional
    protected void seedGrupeArtikala() {
        Long idKompanije = kompanijaRepository.findByPib("1234567890")
                .orElseThrow(() -> new IllegalStateException("Kompanija s PIB-om '1234567890' nije pronađena."))
                .getId();

        List<String> postojeceNazive = grupaArtikalaRepository.findByIdKompanijeOrderByNazivAsc(idKompanije)
                .stream()
                .map(GrupaArtikala::getNaziv)
                .toList();

        List<SeedGrupa> grupe = List.of(
                new SeedGrupa("Majice",          "Majice kratkih i dugih rukava"),
                new SeedGrupa("Hlače",           "Hlače svih stilova i materijala"),
                new SeedGrupa("Haljine",         "Haljine za sve prilike"),
                new SeedGrupa("Jakne",           "Jakne i kaputi"),
                new SeedGrupa("Obuća",           "Cipele, čizme i sportska obuća"),
                new SeedGrupa("Donje rublje",    "Donje rublje i pidžame"),
                new SeedGrupa("Sportska odjeća", "Odjeća za sport i rekreaciju"),
                new SeedGrupa("Džemperi",        "Džemperi, kardigani i puloveri"),
                new SeedGrupa("Košulje",         "Košulje i bluze"),
                new SeedGrupa("Accessories",     "Šalovi, kape, torbe i ostali dodaci")
        );

        for (SeedGrupa seed : grupe) {
            if (postojeceNazive.contains(seed.naziv())) {
                log.debug("Grupa artikala '{}' već postoji za kompaniju {} — preskačem.", seed.naziv(), idKompanije);
                continue;
            }
            GrupaArtikala g = new GrupaArtikala();
            g.setIdKompanije(idKompanije);
            g.setNaziv(seed.naziv());
            g.setOpis(seed.opis());
            g.setAktivan(true);
            g.setIdRoditeljskeGrupe(null);
            grupaArtikalaRepository.save(g);
            log.info("Kreirana grupa artikala: '{}'", seed.naziv());
        }
    }

    @Transactional
    protected void seedArtikliKompanije() {
        if (artikalKompanijeRepository.count() > 0) {
            return;
        }

        Long idKompanije = kompanijaRepository.findByPib("1234567890")
                .orElseThrow(() -> new IllegalStateException("Kompanija s PIB-om '1234567890' nije pronađena."))
                .getId();

        List<GrupaArtikala> grupe = grupaArtikalaRepository.findAll();
        Long idGrupaMajice   = grupe.stream().filter(g -> g.getNaziv().equals("Majice")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Grupa 'Majice' nije pronađena.")).getId();
        Long idGrupaHlace    = grupe.stream().filter(g -> g.getNaziv().equals("Hlače")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Grupa 'Hlače' nije pronađena.")).getId();
        Long idGrupaHaljine  = grupe.stream().filter(g -> g.getNaziv().equals("Haljine")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Grupa 'Haljine' nije pronađena.")).getId();
        Long idGrupaJakne    = grupe.stream().filter(g -> g.getNaziv().equals("Jakne")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Grupa 'Jakne' nije pronađena.")).getId();
        Long idGrupaDzemperi = grupe.stream().filter(g -> g.getNaziv().equals("Džemperi")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Grupa 'Džemperi' nije pronađena.")).getId();
        Long idGrupaObuca    = grupe.stream().filter(g -> g.getNaziv().equals("Obuća")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Grupa 'Obuća' nije pronađena.")).getId();

        List<Proizvodjac> proizvodjaci = proizvodjacRepository.findAll();
        Long idZara    = proizvodjaci.stream().filter(p -> p.getNaziv().equals("Zara (Inditex)")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Proizvođač 'Zara (Inditex)' nije pronađen.")).getId();
        Long idHM      = proizvodjaci.stream().filter(p -> p.getNaziv().equals("H&M Group")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Proizvođač 'H&M Group' nije pronađen.")).getId();
        Long idMango   = proizvodjaci.stream().filter(p -> p.getNaziv().equals("Mango")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Proizvođač 'Mango' nije pronađen.")).getId();
        Long idNike    = proizvodjaci.stream().filter(p -> p.getNaziv().equals("Nike Inc.")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Proizvođač 'Nike Inc.' nije pronađen.")).getId();
        Long idAdidas  = proizvodjaci.stream().filter(p -> p.getNaziv().equals("Adidas AG")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Proizvođač 'Adidas AG' nije pronađen.")).getId();
        Long idLevis   = proizvodjaci.stream().filter(p -> p.getNaziv().equals("Levi Strauss & Co.")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Proizvođač 'Levi Strauss & Co.' nije pronađen.")).getId();

        List<Dobavljac> dobavljaci = dobavljacRepository.findAll();
        Long idTekstilImport = dobavljaci.stream().filter(d -> d.getNaziv().equals("Tekstil Import d.o.o.")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Dobavljač 'Tekstil Import d.o.o.' nije pronađen.")).getId();
        Long idFashionTrade  = dobavljaci.stream().filter(d -> d.getNaziv().equals("Fashion Trade d.o.o.")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Dobavljač 'Fashion Trade d.o.o.' nije pronađen.")).getId();
        Long idEuroTekstil   = dobavljaci.stream().filter(d -> d.getNaziv().equals("Euro Tekstil d.o.o.")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Dobavljač 'Euro Tekstil d.o.o.' nije pronađen.")).getId();
        Long idGlobalFashion = dobavljaci.stream().filter(d -> d.getNaziv().equals("Global Fashion d.o.o.")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Dobavljač 'Global Fashion d.o.o.' nije pronađen.")).getId();

        List<TipVelicina> tipoviVelicina = tipVelicinaRepository.findAll();
        Long idTipaAbecedni  = tipoviVelicina.stream().filter(t -> t.getNaziv().equals("Konfekcija (abecedni)")).findFirst()
                .map(TipVelicina::getId).orElse(null);
        Long idTipaNumericki = tipoviVelicina.stream().filter(t -> t.getNaziv().equals("Konfekcija (numerički)")).findFirst()
                .map(TipVelicina::getId).orElse(null);
        Long idTipaObuca     = tipoviVelicina.stream().filter(t -> t.getNaziv().equals("Čarape/Čizme")).findFirst()
                .map(TipVelicina::getId).orElse(null);

        // Majice — Konfekcija (abecedni)
        saveArtikalKompanije(idKompanije, "Majica kratkih rukava bijela", "MAJ-001", "kom", new BigDecimal("17.00"), idGrupaMajice,   idZara,   idTekstilImport, idTipaAbecedni);
        saveArtikalKompanije(idKompanije, "Majica polo crna",             "MAJ-002", "kom", new BigDecimal("17.00"), idGrupaMajice,   idHM,     idEuroTekstil,   idTipaAbecedni);
        saveArtikalKompanije(idKompanije, "Sportska majica Dri-FIT",      "MAJ-003", "kom", new BigDecimal("17.00"), idGrupaMajice,   idNike,   idTekstilImport, idTipaAbecedni);
        saveArtikalKompanije(idKompanije, "Oversized majica siva",        "MAJ-004", "kom", new BigDecimal("17.00"), idGrupaMajice,   idMango,  idFashionTrade,  idTipaAbecedni);

        // Hlače — Konfekcija (abecedni)
        saveArtikalKompanije(idKompanije, "Chino hlače bež",              "HLA-001", "kom", new BigDecimal("17.00"), idGrupaHlace,    idZara,   idFashionTrade,  idTipaAbecedni);
        saveArtikalKompanije(idKompanije, "Sportske hlače Track",         "HLA-002", "kom", new BigDecimal("17.00"), idGrupaHlace,    idAdidas, idEuroTekstil,   idTipaAbecedni);

        // Traperice — Konfekcija (numerički)
        saveArtikalKompanije(idKompanije, "Traperice Slim Fit plave",     "TRA-001", "kom", new BigDecimal("17.00"), idGrupaHlace,    idLevis,  idGlobalFashion, idTipaNumericki);
        saveArtikalKompanije(idKompanije, "Traperice Wide Leg crne",      "TRA-002", "kom", new BigDecimal("17.00"), idGrupaHlace,    idMango,  idGlobalFashion, idTipaNumericki);

        // Haljine — Konfekcija (abecedni)
        saveArtikalKompanije(idKompanije, "Ljetna haljina cvjetna",       "HAL-001", "kom", new BigDecimal("17.00"), idGrupaHaljine,  idMango,  idFashionTrade,  idTipaAbecedni);
        saveArtikalKompanije(idKompanije, "Mini haljina crna",            "HAL-002", "kom", new BigDecimal("17.00"), idGrupaHaljine,  idZara,   idTekstilImport, idTipaAbecedni);

        // Jakne — Konfekcija (abecedni)
        saveArtikalKompanije(idKompanije, "Zimska jakna puffer crna",     "JAK-001", "kom", new BigDecimal("17.00"), idGrupaJakne,    idHM,     idEuroTekstil,   idTipaAbecedni);
        saveArtikalKompanije(idKompanije, "Sportska jakna Tiro",          "JAK-002", "kom", new BigDecimal("17.00"), idGrupaJakne,    idAdidas, idEuroTekstil,   idTipaAbecedni);

        // Džemperi — Konfekcija (abecedni)
        saveArtikalKompanije(idKompanije, "Džemper vuneni sivi",          "DZE-001", "kom", new BigDecimal("17.00"), idGrupaDzemperi, idHM,     idTekstilImport, idTipaAbecedni);

        // Obuća — Čarape/Čizme
        saveArtikalKompanije(idKompanije, "Tenisice Air Max bijele",      "OBU-001", "kom", new BigDecimal("17.00"), idGrupaObuca,    idNike,   idGlobalFashion, idTipaObuca);
        saveArtikalKompanije(idKompanije, "Patike Superstar bijele",      "OBU-002", "kom", new BigDecimal("17.00"), idGrupaObuca,    idAdidas, idGlobalFashion, idTipaObuca);
    }

    private ArtikalKompanija saveArtikalKompanije(Long idKompanije, String naziv, String sifra, String jedin,
                                                   BigDecimal pdv, Long idGrupe, Long idProizvodjaca,
                                                   Long idDobavljaca, Long idTipaVelicina) {
        ArtikalKompanija a = new ArtikalKompanija();
        a.setIdKompanije(idKompanije);
        a.setNaziv(naziv);
        a.setSifra(sifra);
        a.setJedin(jedin);
        a.setPdv(pdv);
        a.setIdGrupe(idGrupe);
        a.setIdProizvodjaca(idProizvodjaca);
        a.setIdDobavljaca(idDobavljaca);
        a.setIdTipaVelicina(idTipaVelicina);
        a.setAktivan(true);
        ArtikalKompanija saved = artikalKompanijeRepository.save(a);
        log.info("Kreiran artikal kompanije: {} ({})", naziv, sifra);

        // Kreira default varijantu (bez veličine) za svaki novi artikal
        VarijantaArtikla varijanta = new VarijantaArtikla();
        varijanta.setIdArtikla(saved.getId());
        varijanta.setIdVelicine(null);
        varijanta.setAktivan(true);
        varijanta.setIdKompanije(idKompanije);
        varijantaArtiklaRepository.save(varijanta);

        // Kreira zapis zalihe (kolicina=0) za svaku poslovnicu kompanije
        poslovnicaRepository.findByIdKompanije(idKompanije).forEach(poslovnica -> {
            VarijantaArtiklaPoslovnica vap = new VarijantaArtiklaPoslovnica();
            vap.setIdVarijante(varijanta.getId());
            vap.setIdPoslovnice(poslovnica.getId());
            vap.setIdKompanije(idKompanije);
            vap.setKolicina(BigDecimal.ZERO);
            varijantaArtiklaPoslovnicaRepository.save(vap);
        });

        return saved;
    }

    @Transactional
    protected void seedArtikliPoslovnice() {
        if (artikalPoslovnicaRepository.count() > 0) {
            return;
        }

        Long idKompanije = kompanijaRepository.findByPib("1234567890")
                .orElseThrow(() -> new IllegalStateException("Kompanija s PIB-om '1234567890' nije pronađena."))
                .getId();

        Long idPoslovnice = poslovnicaRepository.findByNazivAndIdKompanije("Centrala", idKompanije)
                .orElseThrow(() -> new IllegalStateException("Poslovnica 'Centrala' nije pronađena za kompaniju " + idKompanije))
                .getId();

        List<ArtikalKompanija> artikli = artikalKompanijeRepository.findAll();
        List<ArtikalKompanija> prvih5 = artikli.stream()
                .sorted((a, b) -> a.getSifra().compareTo(b.getSifra()))
                .limit(5)
                .toList();

        BigDecimal[] cijene = {
            new BigDecimal("850.00"),
            new BigDecimal("620.00"),
            new BigDecimal("490.00"),
            new BigDecimal("180.00"),
            new BigDecimal("75.00")
        };

        for (int i = 0; i < prvih5.size(); i++) {
            ArtikalKompanija artikal = prvih5.get(i);
            BigDecimal vpc = cijene[i];
            BigDecimal mpc = vpc.multiply(new BigDecimal("1.20")).setScale(4, java.math.RoundingMode.HALF_UP);

            ArtikalPoslovnica ap = new ArtikalPoslovnica();
            ap.setIdKompanije(idKompanije);
            ap.setIdPoslovnice(idPoslovnice);
            ap.setIdArtikla(artikal.getId());
            ap.setVpc(vpc);
            ap.setMarza(new BigDecimal("20.00"));
            ap.setTipMarze(TipMarze.SLOBODNA);
            ap.setMpc(mpc);
            ap.setAktivan(true);
            artikalPoslovnicaRepository.save(ap);
            log.info("Kreiran artikal poslovnice: artikal='{}', poslovnica={}, vpc={}, mpc={}",
                    artikal.getNaziv(), idPoslovnice, vpc, mpc);
        }
    }

    @Transactional
    protected void seedTipoviVelicina() {
        Long idKompanije = kompanijaRepository.findByPib("1234567890")
                .orElseThrow(() -> new IllegalStateException("Kompanija s PIB-om '1234567890' nije pronađena."))
                .getId();

        seedTipVelicina(idKompanije, "Konfekcija (abecedni)",
                "Standardne abecedne konfekcijske veličine",
                List.of("XS", "S", "M", "L", "XL", "XXL", "3XL"));

        seedTipVelicina(idKompanije, "Konfekcija (numerički)",
                "Standardne numeričke konfekcijske veličine",
                List.of("36", "38", "40", "42", "44", "46", "48"));

        seedTipVelicina(idKompanije, "Dječija (cm)",
                "Dječije veličine izražene u centimetrima",
                List.of("86", "92", "98", "104", "110", "116", "122", "128"));

        seedTipVelicina(idKompanije, "Čarape/Čizme",
                "Veličine za čarape i čizme u rasponu brojeva",
                List.of("36/37", "38/39", "40/41", "42/43", "44/45"));
    }

    private void seedTipVelicina(Long idKompanije, String naziv, String opis, List<String> oznake) {
        if (tipVelicinaRepository.existsByNazivAndIdKompanije(naziv, idKompanije)) {
            log.debug("Tip veličine '{}' već postoji za kompaniju {} — preskačem.", naziv, idKompanije);
            return;
        }

        TipVelicina tip = new TipVelicina();
        tip.setIdKompanije(idKompanije);
        tip.setNaziv(naziv);
        tip.setOpis(opis);
        tip.setAktivan(true);
        TipVelicina savedTip = tipVelicinaRepository.save(tip);
        log.info("Kreiran tip veličine: '{}'", naziv);

        for (int i = 0; i < oznake.size(); i++) {
            Velicina v = new Velicina();
            v.setIdKompanije(idKompanije);
            v.setIdTipaVelicina(savedTip.getId());
            v.setOznaka(oznake.get(i));
            v.setRedosljed(i + 1);
            v.setAktivan(true);
            velicinaRepository.save(v);
        }
        log.info("Kreirano {} veličina za tip '{}'.", oznake.size(), naziv);
    }

    @Transactional
    protected void seedBoje() {
        Long idKompanije = kompanijaRepository.findByPib("1234567890")
                .orElseThrow(() -> new IllegalStateException("Kompanija s PIB-om '1234567890' nije pronađena."))
                .getId();

        List<SeedBoja> boje = List.of(
                new SeedBoja("Crvena",     "#FF0000"),
                new SeedBoja("Plava",      "#0000FF"),
                new SeedBoja("Zelena",     "#008000"),
                new SeedBoja("Crna",       "#000000"),
                new SeedBoja("Bijela",     "#FFFFFF"),
                new SeedBoja("Žuta",       "#FFFF00"),
                new SeedBoja("Narandžasta","#FF6600"),
                new SeedBoja("Roza",       "#FF69B4"),
                new SeedBoja("Siva",       "#808080"),
                new SeedBoja("Bež",        "#F5F5DC")
        );

        for (SeedBoja seed : boje) {
            if (bojaRepository.existsByNazivAndIdKompanije(seed.naziv(), idKompanije)) {
                log.debug("Boja '{}' već postoji za kompaniju {} — preskačem.", seed.naziv(), idKompanije);
                continue;
            }
            Boja b = new Boja();
            b.setIdKompanije(idKompanije);
            b.setNaziv(seed.naziv());
            b.setHexKod(seed.hexKod());
            b.setAktivan(true);
            bojaRepository.save(b);
            log.info("Kreirana boja: '{}' ({})", seed.naziv(), seed.hexKod());
        }
    }

    @Transactional
    protected void seedDefinicijeAtributa() {
        Long idKompanije = kompanijaRepository.findByPib("1234567890")
                .orElseThrow(() -> new IllegalStateException("Kompanija s PIB-om '1234567890' nije pronađena."))
                .getId();

        seedDefinicijaAtributa(idKompanije, "Fit",       1, false, false,
                List.of("Regular", "Slim", "Relaxed", "Oversized", "Skinny"));

        seedDefinicijaAtributa(idKompanije, "Sezona",    2, false, true,
                List.of("Proljeće/Ljeto", "Jesen/Zima", "Sve sezone"));

        seedDefinicijaAtributa(idKompanije, "Spol",      3, false, true,
                List.of("Muški", "Ženski", "Unisex", "Dječiji"));

        seedDefinicijaAtributa(idKompanije, "Materijal", 4, false, true,
                List.of("100% Pamuk", "Pamuk/Poliester", "100% Vuna", "Viskoze", "Denim", "Koža", "Sintetika"));
    }

    private void seedDefinicijaAtributa(Long idKompanije, String naziv, int redosljed,
                                         boolean obavezno, boolean zaWeb, List<String> vrijednosti) {
        if (definicijaAtributaRepository.existsByNazivAndIdKompanije(naziv, idKompanije)) {
            log.debug("Definicija atributa '{}' već postoji za kompaniju {} — preskačem.", naziv, idKompanije);
            return;
        }

        DefinicijaAtributa def = new DefinicijaAtributa();
        def.setIdKompanije(idKompanije);
        def.setNaziv(naziv);
        def.setRedosljed(redosljed);
        def.setObavezno(obavezno);
        def.setZaWeb(zaWeb);
        def.setAktivan(true);
        DefinicijaAtributa savedDef = definicijaAtributaRepository.save(def);
        log.info("Kreirana definicija atributa: '{}' (redosljed={}, zaWeb={})", naziv, redosljed, zaWeb);

        for (int i = 0; i < vrijednosti.size(); i++) {
            VrijednostAtributa va = new VrijednostAtributa();
            va.setIdKompanije(idKompanije);
            va.setIdDefinicije(savedDef.getId());
            va.setVrijednost(vrijednosti.get(i));
            va.setRedosljed(i + 1);
            va.setAktivan(true);
            vrijednostAtributaRepository.save(va);
        }
        log.info("Kreirano {} vrijednosti za atribut '{}'.", vrijednosti.size(), naziv);
    }

    @Transactional
    protected void seedTipoviDokumenata() {
        List<ba.maloprodaja.kompanija.entity.Kompanija> kompanije = kompanijaRepository.findAll();

        record SeedTip(String kod, String naziv, short smjer) {}

        List<SeedTip> tipovi = List.of(
                new SeedTip("UF",   "Ulazna faktura",          (short)  1),
                new SeedTip("PD",   "Povrat dobavljaču",       (short) -1),
                new SeedTip("IF",   "Izlazna faktura",         (short) -1),
                new SeedTip("MSI",  "Međuskladišnica - izlaz", (short) -1),
                new SeedTip("MSU",  "Međuskladišnica - ulaz",  (short)  1),
                new SeedTip("PROD", "Prodaja (blagajna)",      (short) -1)
        );

        for (ba.maloprodaja.kompanija.entity.Kompanija kompanija : kompanije) {
            Long idKompanije = kompanija.getId();
            for (SeedTip seed : tipovi) {
                if (tipDokumentaRepository.existsByKodAndIdKompanije(seed.kod(), idKompanije)) {
                    log.debug("Tip dokumenta '{}' već postoji za kompaniju {} — preskačem.", seed.kod(), idKompanije);
                    continue;
                }
                TipDokumenta tip = new TipDokumenta();
                tip.setKod(seed.kod());
                tip.setNaziv(seed.naziv());
                tip.setSmjerKolicine(seed.smjer());
                tip.setIdKompanije(idKompanije);
                tipDokumentaRepository.save(tip);
                log.info("Kreiran tip dokumenta: '{}' ({}) za kompaniju {}", seed.naziv(), seed.kod(), idKompanije);
            }
        }
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

    private record SeedBoja(String naziv, String hexKod) {}

    private record SeedGrupa(String naziv, String opis) {}
}
