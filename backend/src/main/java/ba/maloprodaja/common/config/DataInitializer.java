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
import ba.maloprodaja.sifarnici.dobavljac.entity.Dobavljac;
import ba.maloprodaja.sifarnici.dobavljac.repository.DobavljacRepository;
import ba.maloprodaja.sifarnici.grupaartikala.entity.GrupaArtikala;
import ba.maloprodaja.sifarnici.grupaartikala.repository.GrupaArtikalaRepository;
import ba.maloprodaja.sifarnici.kupac.entity.Kupac;
import ba.maloprodaja.sifarnici.kupac.repository.KupacRepository;
import ba.maloprodaja.sifarnici.proizvodjac.entity.Proizvodjac;
import ba.maloprodaja.sifarnici.proizvodjac.repository.ProizvodjacRepository;
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

    @Override
    public void run(ApplicationArguments args) {
        seedKompanije();
        seedUsers();
        seedProizvodjaci();
        seedDobavljaci();
        seedKupci();
        seedGrupeArtikala();
        seedArtikliKompanije();
        seedArtikliPoslovnice();
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
        p1.setNaziv("Philips");
        p1.setDrzava("Nizozemska");
        p1.setKontaktOsoba("Jan de Vries");
        p1.setTelefon("+31 20 123 4567");
        p1.setEmail("contact@philips.com");
        p1.setAktivan(true);
        proizvodjacRepository.save(p1);
        log.info("Kreiran proizvođač: Philips");

        Proizvodjac p2 = new Proizvodjac();
        p2.setIdKompanije(idKompanije);
        p2.setNaziv("Samsung");
        p2.setDrzava("Južna Koreja");
        p2.setKontaktOsoba("Kim Jae Won");
        p2.setTelefon("+82 2 2255 0114");
        p2.setEmail("contact@samsung.com");
        p2.setAktivan(true);
        proizvodjacRepository.save(p2);
        log.info("Kreiran proizvođač: Samsung");

        Proizvodjac p3 = new Proizvodjac();
        p3.setIdKompanije(idKompanije);
        p3.setNaziv("LG Electronics");
        p3.setDrzava("Južna Koreja");
        p3.setKontaktOsoba("Park Min Ho");
        p3.setTelefon("+82 2 3777 1114");
        p3.setEmail("contact@lg.com");
        p3.setAktivan(true);
        proizvodjacRepository.save(p3);
        log.info("Kreiran proizvođač: LG Electronics");
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
        d1.setNaziv("Elektronika d.o.o.");
        d1.setAdresa("Zmaja od Bosne 12");
        d1.setGrad("Sarajevo");
        d1.setTelefon("033 445 566");
        d1.setEmail("nabavka@elektronika.ba");
        d1.setPib("1111111111");
        d1.setAktivan(true);
        dobavljacRepository.save(d1);
        log.info("Kreiran dobavljač: Elektronika d.o.o.");

        Dobavljac d2 = new Dobavljac();
        d2.setIdKompanije(idKompanije);
        d2.setNaziv("Tehno Trade d.o.o.");
        d2.setAdresa("Bulevar Meše Selimovića 5");
        d2.setGrad("Sarajevo");
        d2.setTelefon("033 778 899");
        d2.setEmail("info@tehnotrade.ba");
        d2.setPib("2222222222");
        d2.setAktivan(true);
        dobavljacRepository.save(d2);
        log.info("Kreiran dobavljač: Tehno Trade d.o.o.");

        Dobavljac d3 = new Dobavljac();
        d3.setIdKompanije(idKompanije);
        d3.setNaziv("GlobalSupply d.o.o.");
        d3.setAdresa("Rondo 8");
        d3.setGrad("Mostar");
        d3.setTelefon("036 321 654");
        d3.setEmail("supply@globalsupply.ba");
        d3.setPib("3333333333");
        d3.setAktivan(true);
        dobavljacRepository.save(d3);
        log.info("Kreiran dobavljač: GlobalSupply d.o.o.");

        Dobavljac d4 = new Dobavljac();
        d4.setIdKompanije(idKompanije);
        d4.setNaziv("ProDistribucija d.o.o.");
        d4.setAdresa("Titova 44");
        d4.setGrad("Banja Luka");
        d4.setTelefon("051 987 654");
        d4.setEmail("distribucija@prodist.ba");
        d4.setPib("4444444444");
        d4.setAktivan(true);
        dobavljacRepository.save(d4);
        log.info("Kreiran dobavljač: ProDistribucija d.o.o.");
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
        if (grupaArtikalaRepository.count() > 0) {
            return;
        }

        Long idKompanije = kompanijaRepository.findByPib("1234567890")
                .orElseThrow(() -> new IllegalStateException("Kompanija s PIB-om '1234567890' nije pronađena."))
                .getId();

        GrupaArtikala g1 = new GrupaArtikala();
        g1.setIdKompanije(idKompanije);
        g1.setNaziv("Bijela tehnika");
        g1.setOpis("Veći kućanski aparati: hladnjaci, perilice, sušilice");
        g1.setAktivan(true);
        g1.setIdRoditeljskeGrupe(null);
        grupaArtikalaRepository.save(g1);
        log.info("Kreirana grupa artikala: Bijela tehnika");

        GrupaArtikala g2 = new GrupaArtikala();
        g2.setIdKompanije(idKompanije);
        g2.setNaziv("Mala kućanska elektronika");
        g2.setOpis("Manji kućanski aparati: usisivači, tostere, mikseri");
        g2.setAktivan(true);
        g2.setIdRoditeljskeGrupe(null);
        grupaArtikalaRepository.save(g2);
        log.info("Kreirana grupa artikala: Mala kućanska elektronika");

        GrupaArtikala g3 = new GrupaArtikala();
        g3.setIdKompanije(idKompanije);
        g3.setNaziv("Audio i video tehnika");
        g3.setOpis("Televizori, zvučnici, projektori i oprema za zabavu");
        g3.setAktivan(true);
        g3.setIdRoditeljskeGrupe(null);
        grupaArtikalaRepository.save(g3);
        log.info("Kreirana grupa artikala: Audio i video tehnika");
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
        Long idGrupaBijela  = grupe.stream().filter(g -> g.getNaziv().equals("Bijela tehnika")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Grupa 'Bijela tehnika' nije pronađena.")).getId();
        Long idGrupaMala    = grupe.stream().filter(g -> g.getNaziv().equals("Mala kućanska elektronika")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Grupa 'Mala kućanska elektronika' nije pronađena.")).getId();
        Long idGrupaAV      = grupe.stream().filter(g -> g.getNaziv().equals("Audio i video tehnika")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Grupa 'Audio i video tehnika' nije pronađena.")).getId();

        List<Proizvodjac> proizvodjaci = proizvodjacRepository.findAll();
        Long idPhilips  = proizvodjaci.stream().filter(p -> p.getNaziv().equals("Philips")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Proizvođač 'Philips' nije pronađen.")).getId();
        Long idSamsung  = proizvodjaci.stream().filter(p -> p.getNaziv().equals("Samsung")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Proizvođač 'Samsung' nije pronađen.")).getId();
        Long idLG       = proizvodjaci.stream().filter(p -> p.getNaziv().equals("LG Electronics")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Proizvođač 'LG Electronics' nije pronađen.")).getId();

        List<Dobavljac> dobavljaci = dobavljacRepository.findAll();
        Long idDobavljac1 = dobavljaci.stream().filter(d -> d.getNaziv().equals("Elektronika d.o.o.")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Dobavljač 'Elektronika d.o.o.' nije pronađen.")).getId();
        Long idDobavljac2 = dobavljaci.stream().filter(d -> d.getNaziv().equals("Tehno Trade d.o.o.")).findFirst()
                .orElseThrow(() -> new IllegalStateException("Dobavljač 'Tehno Trade d.o.o.' nije pronađen.")).getId();

        saveArtikalKompanije(idKompanije, "Hladnjak Samsung 300L",        "ART-001", "kom", new BigDecimal("17.00"), idGrupaBijela,  idSamsung,  idDobavljac1);
        saveArtikalKompanije(idKompanije, "Perilica rublja LG 7kg",        "ART-002", "kom", new BigDecimal("17.00"), idGrupaBijela,  idLG,       idDobavljac1);
        saveArtikalKompanije(idKompanije, "Sušilica Philips 8kg",          "ART-003", "kom", new BigDecimal("17.00"), idGrupaBijela,  idPhilips,  idDobavljac2);
        saveArtikalKompanije(idKompanije, "Usisivač Samsung Cyclone",      "ART-004", "kom", new BigDecimal("17.00"), idGrupaMala,    idSamsung,  idDobavljac2);
        saveArtikalKompanije(idKompanije, "Mikser Philips 500W",           "ART-005", "kom", new BigDecimal("17.00"), idGrupaMala,    idPhilips,  idDobavljac1);
        saveArtikalKompanije(idKompanije, "Toster LG 2 proreza",          "ART-006", "kom", new BigDecimal("17.00"), idGrupaMala,    idLG,       idDobavljac2);
        saveArtikalKompanije(idKompanije, "Televizor Samsung QLED 55\"",   "ART-007", "kom", new BigDecimal("17.00"), idGrupaAV,      idSamsung,  idDobavljac1);
        saveArtikalKompanije(idKompanije, "Televizor LG OLED 65\"",        "ART-008", "kom", new BigDecimal("17.00"), idGrupaAV,      idLG,       idDobavljac2);
        saveArtikalKompanije(idKompanije, "Soundbar Philips 200W",         "ART-009", "kom", new BigDecimal("17.00"), idGrupaAV,      idPhilips,  idDobavljac1);
        saveArtikalKompanije(idKompanije, "Projektor Samsung Smart 4K",    "ART-010", "kom", new BigDecimal("17.00"), idGrupaAV,      idSamsung,  idDobavljac2);
    }

    private void saveArtikalKompanije(Long idKompanije, String naziv, String sifra, String jedin,
                                      BigDecimal pdv, Long idGrupe, Long idProizvodjaca, Long idDobavljaca) {
        ArtikalKompanija a = new ArtikalKompanija();
        a.setIdKompanije(idKompanije);
        a.setNaziv(naziv);
        a.setSifra(sifra);
        a.setJedin(jedin);
        a.setPdv(pdv);
        a.setIdGrupe(idGrupe);
        a.setIdProizvodjaca(idProizvodjaca);
        a.setIdDobavljaca(idDobavljaca);
        a.setAktivan(true);
        artikalKompanijeRepository.save(a);
        log.info("Kreiran artikal kompanije: {} ({})", naziv, sifra);
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
            ap.setKolicina(new BigDecimal("10.00"));
            ap.setAktivan(true);
            artikalPoslovnicaRepository.save(ap);
            log.info("Kreiran artikal poslovnice: artikal='{}', poslovnica={}, vpc={}, mpc={}",
                    artikal.getNaziv(), idPoslovnice, vpc, mpc);
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
}
