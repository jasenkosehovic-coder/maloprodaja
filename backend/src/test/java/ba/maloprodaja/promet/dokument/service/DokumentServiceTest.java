package ba.maloprodaja.promet.dokument.service;

import ba.maloprodaja.common.exception.BusinessException;
import ba.maloprodaja.dokumenti.enums.VrstaNivelacije;
import ba.maloprodaja.dokumenti.nivelacija.service.INivelacijaService;
import ba.maloprodaja.promet.brojac.service.BrojacService;
import ba.maloprodaja.promet.dokument.dto.DokumentDTO;
import ba.maloprodaja.promet.dokument.entity.Dokument;
import ba.maloprodaja.promet.dokument.entity.StatusDokumenta;
import ba.maloprodaja.promet.dokument.repository.DokumentRepository;
import ba.maloprodaja.promet.stavka.dto.StavkaDTO;
import ba.maloprodaja.promet.stavka.entity.StavkaDokumenta;
import ba.maloprodaja.promet.stavka.repository.StavkaRepository;
import ba.maloprodaja.promet.tipdokumenta.entity.TipDokumenta;
import ba.maloprodaja.promet.tipdokumenta.repository.TipDokumentaRepository;
import ba.maloprodaja.poslovnica.entity.Poslovnica;
import ba.maloprodaja.poslovnica.repository.PoslovnicaRepository;
import ba.maloprodaja.sifarnici.dobavljac.repository.DobavljacRepository;
import ba.maloprodaja.sifarnici.varijanta.entity.VarijantaArtikla;
import ba.maloprodaja.sifarnici.varijanta.entity.VarijantaArtiklaPoslovnica;
import ba.maloprodaja.sifarnici.varijanta.repository.VarijantaArtiklaRepository;
import ba.maloprodaja.sifarnici.varijanta.repository.VarijantaArtiklaPoslovnicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DokumentServiceTest {

    @Mock private DokumentRepository dokumentRepository;
    @Mock private StavkaRepository stavkaRepository;
    @Mock private TipDokumentaRepository tipDokumentaRepository;
    @Mock private DobavljacRepository dobavljacRepository;
    @Mock private PoslovnicaRepository poslovnicaRepository;
    @Mock private VarijantaArtiklaRepository varijantaArtiklaRepository;
    @Mock private VarijantaArtiklaPoslovnicaRepository varijantaPoslovnicaRepository;
    @Mock private BrojacService brojacService;
    @Mock private INivelacijaService nivelacijaService;

    @InjectMocks
    private DokumentService dokumentService;

    private static final Long KOMPANIJA_ID = 1L;
    private static final Long POSLOVNICA_ID = 10L;
    private static final Long VARIJANTA_ID  = 100L;
    private static final Long ARTIKAL_ID    = 200L;

    private TipDokumenta tipUF;
    private TipDokumenta tipMSI;
    private TipDokumenta tipMSU;
    private Poslovnica poslovnica;
    private VarijantaArtikla varijanta;

    @BeforeEach
    void setUp() {
        tipUF = buildTip(1L, "UF", "Ulazna faktura", (short) 1);
        tipMSI = buildTip(2L, "MSI", "Međuskladišnica izlaz", (short) -1);
        tipMSU = buildTip(3L, "MSU", "Međuskladišnica ulaz", (short) 1);

        poslovnica = new Poslovnica();
        poslovnica.setId(POSLOVNICA_ID);
        poslovnica.setNaziv("Test poslovnica");

        varijanta = new VarijantaArtikla();
        varijanta.setId(VARIJANTA_ID);
        varijanta.setIdArtikla(ARTIKAL_ID);
        varijanta.setIdKompanije(KOMPANIJA_ID);
    }

    // -------------------------------------------------------------------------
    // create
    // -------------------------------------------------------------------------

    @Test
    void create_shouldCreateNacrtDocument() {
        DokumentDTO.CreateDokumentDTO dto = new DokumentDTO.CreateDokumentDTO(
                tipUF.getId(), POSLOVNICA_ID, null, LocalDate.now(), "Napomena", List.of()
        );

        Dokument savedDokument = buildDokument(42L, tipUF, POSLOVNICA_ID, StatusDokumenta.NACRT);

        when(tipDokumentaRepository.findById(tipUF.getId())).thenReturn(Optional.of(tipUF));
        when(poslovnicaRepository.findById(POSLOVNICA_ID)).thenReturn(Optional.of(poslovnica));
        when(dokumentRepository.save(any(Dokument.class))).thenReturn(savedDokument);

        DokumentDTO.DokumentResponseDTO result = dokumentService.create(dto, KOMPANIJA_ID);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(42L);
        assertThat(result.status()).isEqualTo("NACRT");

        ArgumentCaptor<Dokument> captor = ArgumentCaptor.forClass(Dokument.class);
        verify(dokumentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusDokumenta.NACRT);
        assertThat(captor.getValue().getTipDokumenta()).isEqualTo(tipUF);
    }

    // -------------------------------------------------------------------------
    // potvrdi
    // -------------------------------------------------------------------------

    @Test
    void potvrdi_shouldSetStatusPotvrdenAndUpdateStock() {
        StavkaDokumenta stavka = buildStavka(VARIJANTA_ID, new BigDecimal("5.000"), new BigDecimal("10.00"));

        Dokument dokument = buildDokumentWithStavke(1L, tipUF, POSLOVNICA_ID, StatusDokumenta.NACRT,
                List.of(stavka));

        when(dokumentRepository.findById(1L)).thenReturn(Optional.of(dokument));
        when(brojacService.nextBrojDokumenta(tipUF, KOMPANIJA_ID)).thenReturn("UF-2026-0001");
        when(dokumentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(varijantaPoslovnicaRepository.findByIdVarijanteAndIdPoslovnice(VARIJANTA_ID, POSLOVNICA_ID))
                .thenReturn(Optional.empty());
        when(varijantaPoslovnicaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        // For toDokumentResponse mapping
        when(poslovnicaRepository.findById(POSLOVNICA_ID)).thenReturn(Optional.of(poslovnica));
        when(varijantaArtiklaRepository.findById(VARIJANTA_ID)).thenReturn(Optional.of(varijanta));
        // For nivelacija — batch load varijante
        when(varijantaArtiklaRepository.findAllById(anyCollection())).thenReturn(List.of(varijanta));

        DokumentDTO.DokumentResponseDTO result = dokumentService.potvrdi(1L, KOMPANIJA_ID);

        assertThat(result.status()).isEqualTo("POTVRDEN");
        assertThat(result.brojDokumenta()).isEqualTo("UF-2026-0001");

        // Verify stock update was called with the signed kolicina
        ArgumentCaptor<VarijantaArtiklaPoslovnica> zalihaCaptor =
                ArgumentCaptor.forClass(VarijantaArtiklaPoslovnica.class);
        verify(varijantaPoslovnicaRepository).save(zalihaCaptor.capture());
        assertThat(zalihaCaptor.getValue().getKolicina()).isEqualByComparingTo(new BigDecimal("5.000"));
    }

    @Test
    void potvrdi_shouldThrowWhenNoStavke() {
        Dokument dokument = buildDokument(1L, tipUF, POSLOVNICA_ID, StatusDokumenta.NACRT);

        when(dokumentRepository.findById(1L)).thenReturn(Optional.of(dokument));

        assertThatThrownBy(() -> dokumentService.potvrdi(1L, KOMPANIJA_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("bez stavki");
    }

    @Test
    void potvrdi_shouldThrowWhenAlreadyPotvrden() {
        Dokument dokument = buildDokument(1L, tipUF, POSLOVNICA_ID, StatusDokumenta.POTVRDEN);

        when(dokumentRepository.findById(1L)).thenReturn(Optional.of(dokument));

        assertThatThrownBy(() -> dokumentService.potvrdi(1L, KOMPANIJA_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("NACRT");
    }

    @Test
    void potvrdi_shouldTriggerNivelacijaForUF() {
        StavkaDokumenta stavka = buildStavka(VARIJANTA_ID, new BigDecimal("3.000"), new BigDecimal("25.00"));
        Dokument dokument = buildDokumentWithStavke(5L, tipUF, POSLOVNICA_ID, StatusDokumenta.NACRT,
                List.of(stavka));

        when(dokumentRepository.findById(5L)).thenReturn(Optional.of(dokument));
        when(brojacService.nextBrojDokumenta(tipUF, KOMPANIJA_ID)).thenReturn("UF-2026-0002");
        when(dokumentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(varijantaPoslovnicaRepository.findByIdVarijanteAndIdPoslovnice(VARIJANTA_ID, POSLOVNICA_ID))
                .thenReturn(Optional.empty());
        when(varijantaPoslovnicaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(poslovnicaRepository.findById(POSLOVNICA_ID)).thenReturn(Optional.of(poslovnica));
        when(varijantaArtiklaRepository.findById(VARIJANTA_ID)).thenReturn(Optional.of(varijanta));
        when(varijantaArtiklaRepository.findAllById(anyCollection())).thenReturn(List.of(varijanta));

        dokumentService.potvrdi(5L, KOMPANIJA_ID);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<INivelacijaService.NivelacijaStavkaInfo>> stavkeCaptor =
                ArgumentCaptor.forClass((Class) List.class);
        verify(nivelacijaService).kreirajAutomatskuZaDokument(
                eq(5L),
                eq(VrstaNivelacije.AUTOMATSKA_FAKTURA),
                eq(KOMPANIJA_ID),
                eq(POSLOVNICA_ID),
                any(),
                stavkeCaptor.capture()
        );

        List<INivelacijaService.NivelacijaStavkaInfo> infos = stavkeCaptor.getValue();
        assertThat(infos).hasSize(1);
        assertThat(infos.get(0).idArtikla()).isEqualTo(ARTIKAL_ID);
        assertThat(infos.get(0).kolicina()).isEqualByComparingTo(new BigDecimal("3.000"));
        assertThat(infos.get(0).vpc()).isEqualByComparingTo(new BigDecimal("25.00"));
    }

    @Test
    void potvrdi_shouldNotFailWhenNivelacijaThrows() {
        StavkaDokumenta stavka = buildStavka(VARIJANTA_ID, new BigDecimal("2.000"), new BigDecimal("10.00"));
        Dokument dokument = buildDokumentWithStavke(6L, tipUF, POSLOVNICA_ID, StatusDokumenta.NACRT,
                List.of(stavka));

        when(dokumentRepository.findById(6L)).thenReturn(Optional.of(dokument));
        when(brojacService.nextBrojDokumenta(tipUF, KOMPANIJA_ID)).thenReturn("UF-2026-0003");
        when(dokumentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(varijantaPoslovnicaRepository.findByIdVarijanteAndIdPoslovnice(VARIJANTA_ID, POSLOVNICA_ID))
                .thenReturn(Optional.empty());
        when(varijantaPoslovnicaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(poslovnicaRepository.findById(POSLOVNICA_ID)).thenReturn(Optional.of(poslovnica));
        when(varijantaArtiklaRepository.findById(VARIJANTA_ID)).thenReturn(Optional.of(varijanta));
        when(varijantaArtiklaRepository.findAllById(anyCollection())).thenReturn(List.of(varijanta));
        doThrow(new RuntimeException("Nivelacija DB error")).when(nivelacijaService)
                .kreirajAutomatskuZaDokument(any(), any(), any(), any(), any(), any());

        // potvrdi must NOT throw even when nivelacija fails
        DokumentDTO.DokumentResponseDTO result = dokumentService.potvrdi(6L, KOMPANIJA_ID);

        assertThat(result.status()).isEqualTo("POTVRDEN");
        verify(varijantaPoslovnicaRepository).save(any());
    }

    // -------------------------------------------------------------------------
    // storniraj
    // -------------------------------------------------------------------------

    @Test
    void storniraj_shouldCreateNewDocumentWithNegatedKolicina() {
        BigDecimal originalKolicina = new BigDecimal("4.000");
        StavkaDokumenta stavka = buildStavka(VARIJANTA_ID, originalKolicina, new BigDecimal("15.00"));

        Dokument original = buildDokumentWithStavke(10L, tipUF, POSLOVNICA_ID, StatusDokumenta.POTVRDEN,
                List.of(stavka));
        original.setBrojDokumenta("UF-2026-0010");

        // Capture the storno doc so potvrdi() can retrieve it via findById(11L)
        final Dokument[] stornoRef = new Dokument[1];
        when(dokumentRepository.findById(10L)).thenReturn(Optional.of(original));
        when(dokumentRepository.save(any(Dokument.class))).thenAnswer(inv -> {
            Dokument d = inv.getArgument(0);
            if (d.getId() == null) {
                d.setId(11L);
                stornoRef[0] = d;
            }
            return d;
        });
        when(dokumentRepository.findById(11L)).thenAnswer(inv ->
                Optional.ofNullable(stornoRef[0])
        );

        when(brojacService.nextBrojDokumenta(tipUF, KOMPANIJA_ID)).thenReturn("UF-2026-0011");
        when(varijantaPoslovnicaRepository.findByIdVarijanteAndIdPoslovnice(VARIJANTA_ID, POSLOVNICA_ID))
                .thenReturn(Optional.empty());
        when(varijantaPoslovnicaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(poslovnicaRepository.findById(POSLOVNICA_ID)).thenReturn(Optional.of(poslovnica));
        when(varijantaArtiklaRepository.findById(VARIJANTA_ID)).thenReturn(Optional.of(varijanta));
        when(varijantaArtiklaRepository.findAllById(anyCollection())).thenReturn(List.of(varijanta));

        dokumentService.storniraj(10L, KOMPANIJA_ID);

        // The original document must be marked STORNIRAN
        verify(dokumentRepository, atLeastOnce()).save(argThat(d ->
                d.getId() != null && d.getId().equals(10L) &&
                d.getStatus() == StatusDokumenta.STORNIRAN
        ));

        // Stock update must have occurred for the storno stavka (negated kolicina)
        verify(varijantaPoslovnicaRepository, atLeastOnce()).save(any(VarijantaArtiklaPoslovnica.class));
    }

    @Test
    void storniraj_shouldThrowWhenStatusIsNacrt() {
        Dokument dokument = buildDokument(1L, tipUF, POSLOVNICA_ID, StatusDokumenta.NACRT);

        when(dokumentRepository.findById(1L)).thenReturn(Optional.of(dokument));

        assertThatThrownBy(() -> dokumentService.storniraj(1L, KOMPANIJA_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("POTVRDEN");
    }

    // -------------------------------------------------------------------------
    // kreirajMedjuskladisnicu
    // -------------------------------------------------------------------------

    @Test
    void kreirajMedjuskladisnicu_shouldThrowWhenSamePoslovnica() {
        DokumentDTO.CreateMedjuskladisnicaDTO dto = new DokumentDTO.CreateMedjuskladisnicaDTO(
                POSLOVNICA_ID, POSLOVNICA_ID, LocalDate.now(), "Test",
                List.of(buildCreateStavkaDTO())
        );

        assertThatThrownBy(() -> dokumentService.kreirajMedjuskladisnicu(dto, KOMPANIJA_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("različite");
    }

    @Test
    void kreirajMedjuskladisnicu_shouldCreateBothDocuments() {
        Long odredistePoslovnicaId = 20L;

        Poslovnica odredistePoslovnica = new Poslovnica();
        odredistePoslovnica.setId(odredistePoslovnicaId);
        odredistePoslovnica.setNaziv("Odrediste poslovnica");

        DokumentDTO.CreateMedjuskladisnicaDTO dto = new DokumentDTO.CreateMedjuskladisnicaDTO(
                POSLOVNICA_ID, odredistePoslovnicaId, LocalDate.now(), "Medjuskladišnica test",
                List.of(buildCreateStavkaDTO())
        );

        when(tipDokumentaRepository.findByKodAndIdKompanije("MSI", KOMPANIJA_ID))
                .thenReturn(Optional.of(tipMSI));
        when(tipDokumentaRepository.findByKodAndIdKompanije("MSU", KOMPANIJA_ID))
                .thenReturn(Optional.of(tipMSU));
        when(varijantaArtiklaRepository.findById(VARIJANTA_ID)).thenReturn(Optional.of(varijanta));

        // Capture the live document objects so findById can return them with stavke already added
        final Dokument[] msiRef = new Dokument[1];
        final Dokument[] msuRef = new Dokument[1];
        when(dokumentRepository.save(any(Dokument.class))).thenAnswer(inv -> {
            Dokument d = inv.getArgument(0);
            if (d.getId() == null) {
                if (d.getTipDokumenta() == tipMSI) {
                    d.setId(30L);
                    msiRef[0] = d;
                } else if (d.getTipDokumenta() == tipMSU) {
                    d.setId(31L);
                    msuRef[0] = d;
                }
            }
            return d;
        });

        // Return the captured live objects so potvrdi() sees the stavke added in-memory
        when(dokumentRepository.findById(30L)).thenAnswer(inv -> Optional.ofNullable(msiRef[0]));
        when(dokumentRepository.findById(31L)).thenAnswer(inv -> Optional.ofNullable(msuRef[0]));

        when(brojacService.nextBrojDokumenta(eq(tipMSI), eq(KOMPANIJA_ID))).thenReturn("MSI-2026-0001");
        when(brojacService.nextBrojDokumenta(eq(tipMSU), eq(KOMPANIJA_ID))).thenReturn("MSU-2026-0001");
        when(varijantaArtiklaRepository.findAllById(anyCollection())).thenReturn(List.of(varijanta));
        when(varijantaPoslovnicaRepository.findByIdVarijanteAndIdPoslovnice(anyLong(), anyLong()))
                .thenReturn(Optional.empty());
        when(varijantaPoslovnicaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(poslovnicaRepository.findById(POSLOVNICA_ID)).thenReturn(Optional.of(poslovnica));
        when(poslovnicaRepository.findById(odredistePoslovnicaId)).thenReturn(Optional.of(odredistePoslovnica));

        dokumentService.kreirajMedjuskladisnicu(dto, KOMPANIJA_ID);

        // Both document types must have been created
        verify(tipDokumentaRepository).findByKodAndIdKompanije("MSI", KOMPANIJA_ID);
        verify(tipDokumentaRepository).findByKodAndIdKompanije("MSU", KOMPANIJA_ID);
        // Both potvrdi paths must have updated stock (one stavka each)
        verify(varijantaPoslovnicaRepository, times(2)).save(any(VarijantaArtiklaPoslovnica.class));
        // Nivelacija triggered only for MSU (smjer=+1), not for MSI (smjer=-1)
        verify(nivelacijaService, times(1)).kreirajAutomatskuZaDokument(
                eq(31L), eq(VrstaNivelacije.AUTOMATSKA_OTPREMNICA),
                eq(KOMPANIJA_ID), eq(odredistePoslovnicaId), any(), any()
        );
        verify(nivelacijaService, never()).kreirajAutomatskuZaDokument(
                eq(30L), any(), any(), any(), any(), any()
        );
    }

    // -------------------------------------------------------------------------
    // Builders
    // -------------------------------------------------------------------------

    private TipDokumenta buildTip(Long id, String kod, String naziv, short smjer) {
        TipDokumenta tip = new TipDokumenta();
        tip.setId(id);
        tip.setKod(kod);
        tip.setNaziv(naziv);
        tip.setSmjerKolicine(smjer);
        tip.setIdKompanije(KOMPANIJA_ID);
        return tip;
    }

    private Dokument buildDokument(Long id, TipDokumenta tip, Long idPoslovnice, StatusDokumenta status) {
        Dokument d = new Dokument();
        d.setId(id);
        d.setTipDokumenta(tip);
        d.setIdPoslovnice(idPoslovnice);
        d.setStatus(status);
        d.setDatum(LocalDate.now());
        d.setIdKompanije(KOMPANIJA_ID);
        return d;
    }

    private Dokument buildDokumentWithStavke(Long id, TipDokumenta tip, Long idPoslovnice,
                                              StatusDokumenta status, List<StavkaDokumenta> stavke) {
        Dokument d = buildDokument(id, tip, idPoslovnice, status);
        d.getStavke().addAll(stavke);
        return d;
    }

    private StavkaDokumenta buildStavka(Long idVarijante, BigDecimal kolicina, BigDecimal vpc) {
        StavkaDokumenta s = new StavkaDokumenta();
        s.setIdVarijante(idVarijante);
        s.setKolicina(kolicina);
        s.setVpc(vpc);
        s.setPopustProcenat(BigDecimal.ZERO);
        s.setMarzaProcenat(BigDecimal.ZERO);
        s.setPdvProcenat(BigDecimal.ZERO);
        s.setMpc(vpc);
        s.setIznosVpc(vpc.multiply(kolicina));
        s.setIznosMpc(vpc.multiply(kolicina));
        s.setIznosMarze(BigDecimal.ZERO);
        s.setIznosPopusta(BigDecimal.ZERO);
        s.setIznosPdv(BigDecimal.ZERO);
        s.setIdKompanije(KOMPANIJA_ID);
        return s;
    }

    private StavkaDTO.CreateStavkaDTO buildCreateStavkaDTO() {
        return new StavkaDTO.CreateStavkaDTO(VARIJANTA_ID, new BigDecimal("5.000"),
                new BigDecimal("20.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
