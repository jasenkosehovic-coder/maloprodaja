package ba.maloprodaja.auth.service;

import ba.maloprodaja.auth.dto.KorisnikDTO;
import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.auth.entity.KorisnikIzbornik;
import ba.maloprodaja.auth.entity.KorisnikUloga;
import ba.maloprodaja.auth.repository.KorisnikIzborniciRepository;
import ba.maloprodaja.auth.repository.KorisnikRepository;
import ba.maloprodaja.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KorisnikServiceTest {

    @Mock
    private KorisnikRepository korisnikRepository;
    @Mock
    private KorisnikIzborniciRepository korisnikIzborniciRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private KorisnikService korisnikService;

    private Korisnik korisnik;

    @BeforeEach
    void setUp() {
        korisnik = new Korisnik();
        korisnik.setUsername("testuser");
        korisnik.setPasswordHash("hash");
        korisnik.setIme("Test");
        korisnik.setPrezime("Korisnik");
        korisnik.setEmail("test@test.ba");
        korisnik.setUloga(KorisnikUloga.ADMIN);
        korisnik.setAktivan(true);
        korisnik.setIdKompanije(1L);
    }

    @Test
    void listAll_returnsListForKompanija() {
        when(korisnikRepository.findByIdKompanije(1L)).thenReturn(List.of(korisnik));

        List<KorisnikDTO.KorisnikListItemDTO> result = korisnikService.listAll(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).username()).isEqualTo("testuser");
    }

    @Test
    void listAll_emptyList_returnsEmpty() {
        when(korisnikRepository.findByIdKompanije(2L)).thenReturn(List.of());

        List<KorisnikDTO.KorisnikListItemDTO> result = korisnikService.listAll(2L);

        assertThat(result).isEmpty();
    }

    @Test
    void create_savesAndReturnsKorisnik() {
        when(passwordEncoder.encode("pass123")).thenReturn("hashed");
        when(korisnikRepository.save(any())).thenReturn(korisnik);

        var dto = new KorisnikDTO.CreateKorisnikDTO(
                "novuser", "pass123", "Novi", "User", "novi@test.ba", KorisnikUloga.BLAGAJNIK, null
        );

        KorisnikDTO.KorisnikListItemDTO result = korisnikService.create(dto, 1L);

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("testuser");
        verify(korisnikRepository, times(1)).save(any());
    }

    @Test
    void update_existingKorisnik_updatesFields() {
        when(korisnikRepository.findById(1L)).thenReturn(Optional.of(korisnik));
        when(korisnikRepository.save(any())).thenReturn(korisnik);

        var dto = new KorisnikDTO.UpdateKorisnikDTO("NovoIme", null, null, null, null, null, null);
        KorisnikDTO.KorisnikListItemDTO result = korisnikService.update(1L, dto);

        verify(korisnikRepository).save(korisnik);
        assertThat(korisnik.getIme()).isEqualTo("NovoIme");
    }

    @Test
    void update_nonExistingKorisnik_throwsResourceNotFoundException() {
        when(korisnikRepository.findById(99L)).thenReturn(Optional.empty());

        var dto = new KorisnikDTO.UpdateKorisnikDTO(null, null, null, null, null, null, null);

        assertThatThrownBy(() -> korisnikService.update(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deactivate_existingKorisnik_setsAktivanFalse() {
        when(korisnikRepository.findById(1L)).thenReturn(Optional.of(korisnik));
        when(korisnikRepository.save(any())).thenReturn(korisnik);

        korisnikService.deactivate(1L);

        assertThat(korisnik.isAktivan()).isFalse();
        verify(korisnikRepository).save(korisnik);
    }

    @Test
    void deactivate_nonExistingKorisnik_throwsResourceNotFoundException() {
        when(korisnikRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> korisnikService.deactivate(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getIzbornici_returnsIzborniciForKorisnik() {
        KorisnikIzbornik izbornik = new KorisnikIzbornik();
        izbornik.setIzbornikKljuc("PRODAJA");
        izbornik.setAktivan(true);

        when(korisnikIzborniciRepository.findByKorisnikId(1L)).thenReturn(List.of(izbornik));

        List<KorisnikDTO.KorisnikIzbornikaDTO> result = korisnikService.getIzbornici(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).izbornikKljuc()).isEqualTo("PRODAJA");
        assertThat(result.get(0).aktivan()).isTrue();
    }

    @Test
    void updateIzbornici_deletesOldAndSavesNew() {
        when(korisnikRepository.findById(1L)).thenReturn(Optional.of(korisnik));
        when(korisnikIzborniciRepository.saveAll(any())).thenReturn(List.of());
        when(korisnikIzborniciRepository.findByKorisnikId(1L)).thenReturn(List.of());

        List<KorisnikDTO.KorisnikIzbornikaDTO> novi = List.of(
                new KorisnikDTO.KorisnikIzbornikaDTO("PRODAJA", true),
                new KorisnikDTO.KorisnikIzbornikaDTO("IZVJESTAJI", false)
        );

        korisnikService.updateIzbornici(1L, novi);

        verify(korisnikIzborniciRepository).deleteByKorisnikId(1L);
        verify(korisnikIzborniciRepository).saveAll(argThat(list ->
                ((List<?>) list).size() == 2
        ));
    }
}
