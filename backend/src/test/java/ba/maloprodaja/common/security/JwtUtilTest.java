package ba.maloprodaja.common.security;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.auth.entity.KorisnikUloga;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private Korisnik korisnik;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("testSecretKeyThatIsAtLeast32CharactersLongForHmacSha256");
        props.setExpirationMs(3_600_000L);
        props.setRefreshExpirationMs(86_400_000L);

        jwtUtil = new JwtUtil(props);

        korisnik = new Korisnik();
        korisnik.setUsername("testuser");
        korisnik.setPasswordHash("hash");
        korisnik.setUloga(KorisnikUloga.ADMIN);
        korisnik.setAktivan(true);
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtUtil.generateToken(korisnik);
        assertThat(token).isNotBlank();
    }

    @Test
    void extractUsername_returnsCorrectUsername() {
        String token = jwtUtil.generateToken(korisnik);
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("testuser");
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        String token = jwtUtil.generateToken(korisnik);
        assertThat(jwtUtil.validateToken(token, korisnik)).isTrue();
    }

    @Test
    void validateToken_wrongUser_returnsFalse() {
        String token = jwtUtil.generateToken(korisnik);

        Korisnik drugi = new Korisnik();
        drugi.setUsername("drugiuser");
        drugi.setPasswordHash("hash");
        drugi.setUloga(KorisnikUloga.BLAGAJNIK);
        drugi.setAktivan(true);

        assertThat(jwtUtil.validateToken(token, drugi)).isFalse();
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        JwtProperties shortProps = new JwtProperties();
        shortProps.setSecret("testSecretKeyThatIsAtLeast32CharactersLongForHmacSha256");
        shortProps.setExpirationMs(-1000L); // već istekao
        shortProps.setRefreshExpirationMs(86_400_000L);

        JwtUtil shortJwtUtil = new JwtUtil(shortProps);
        String expiredToken = shortJwtUtil.generateToken(korisnik);

        assertThat(shortJwtUtil.validateToken(expiredToken, korisnik)).isFalse();
    }

    @Test
    void generateRefreshToken_returnsNonNullToken() {
        String refreshToken = jwtUtil.generateRefreshToken(korisnik);
        assertThat(refreshToken).isNotBlank();
    }

    @Test
    void extractUsername_fromRefreshToken_returnsCorrectUsername() {
        String refreshToken = jwtUtil.generateRefreshToken(korisnik);
        assertThat(jwtUtil.extractUsername(refreshToken)).isEqualTo("testuser");
    }

    @Test
    void extractUsername_invalidToken_throwsException() {
        assertThatThrownBy(() -> jwtUtil.extractUsername("invalid.token.here"))
                .isInstanceOf(Exception.class);
    }
}
