package ba.maloprodaja.auth.service;

import ba.maloprodaja.auth.dto.LoginRequestDTO;
import ba.maloprodaja.auth.dto.LoginResponseDTO;
import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.auth.entity.KorisnikUloga;
import ba.maloprodaja.auth.repository.KorisnikRepository;
import ba.maloprodaja.common.exception.BusinessException;
import ba.maloprodaja.common.security.JwtProperties;
import ba.maloprodaja.common.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private KorisnikRepository korisnikRepository;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private AuthService authService;

    private Korisnik korisnik;

    @BeforeEach
    void setUp() {
        korisnik = new Korisnik();
        korisnik.setUsername("testuser");
        korisnik.setPasswordHash("encoded_password");
        korisnik.setIme("Test");
        korisnik.setPrezime("Korisnik");
        korisnik.setEmail("test@test.ba");
        korisnik.setUloga(KorisnikUloga.ADMIN);
        korisnik.setAktivan(true);
    }

    @Test
    void login_validCredentials_returnsLoginResponse() {
        var authToken = new UsernamePasswordAuthenticationToken(korisnik, null, korisnik.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authToken);
        when(jwtUtil.generateToken(korisnik)).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(korisnik)).thenReturn("refresh-token");
        when(jwtProperties.getExpirationMs()).thenReturn(3600000L);

        LoginResponseDTO response = authService.login(new LoginRequestDTO("testuser", "password"));

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.korisnik().username()).isEqualTo("testuser");
    }

    @Test
    void login_badCredentials_throwsBadCredentialsException() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Pogrešni kredencijali"));

        assertThatThrownBy(() -> authService.login(new LoginRequestDTO("testuser", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refreshToken_validToken_returnsNewTokens() {
        when(jwtUtil.extractUsername("valid-refresh-token")).thenReturn("testuser");
        when(korisnikRepository.findByUsername("testuser")).thenReturn(Optional.of(korisnik));
        when(jwtUtil.validateToken("valid-refresh-token", korisnik)).thenReturn(true);
        when(jwtUtil.generateToken(korisnik)).thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken(korisnik)).thenReturn("new-refresh-token");
        when(jwtProperties.getExpirationMs()).thenReturn(3600000L);

        LoginResponseDTO response = authService.refreshToken("valid-refresh-token");

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    void refreshToken_invalidToken_throwsBusinessException() {
        when(jwtUtil.extractUsername("bad-token")).thenReturn("testuser");
        when(korisnikRepository.findByUsername("testuser")).thenReturn(Optional.of(korisnik));
        when(jwtUtil.validateToken("bad-token", korisnik)).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken("bad-token"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("nije validan");
    }

    @Test
    void refreshToken_unknownUser_throwsUsernameNotFoundException() {
        when(jwtUtil.extractUsername("token")).thenReturn("nepostojeci");
        when(korisnikRepository.findByUsername("nepostojeci")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken("token"))
                .isInstanceOf(Exception.class);
    }

    @Test
    void me_returnsKorisnikInfo() {
        LoginResponseDTO.KorisnikInfo info = authService.me(korisnik);

        assertThat(info).isNotNull();
        assertThat(info.username()).isEqualTo("testuser");
        assertThat(info.uloga()).isEqualTo(KorisnikUloga.ADMIN);
    }
}
