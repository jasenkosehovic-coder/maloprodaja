package ba.maloprodaja.auth.service;

import ba.maloprodaja.auth.dto.LoginRequestDTO;
import ba.maloprodaja.auth.dto.LoginResponseDTO;
import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.auth.repository.KorisnikRepository;
import ba.maloprodaja.common.exception.BusinessException;
import ba.maloprodaja.common.security.JwtProperties;
import ba.maloprodaja.common.security.JwtUtil;
import ba.maloprodaja.kompanija.entity.Kompanija;
import ba.maloprodaja.kompanija.repository.KompanijaRepository;
import ba.maloprodaja.poslovnica.entity.Poslovnica;
import ba.maloprodaja.poslovnica.repository.PoslovnicaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final KorisnikRepository korisnikRepository;
    private final KompanijaRepository kompanijaRepository;
    private final PoslovnicaRepository poslovnicaRepository;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        Korisnik korisnik = (Korisnik) authentication.getPrincipal();

        if (korisnik.getAktivDo() != null && korisnik.getAktivDo().isBefore(java.time.LocalDate.now())) {
            throw new BusinessException("Korisnička licenca je istekla. Molimo vas kontaktirajte tehničku podršku.");
        }

        String token = jwtUtil.generateToken(korisnik);
        String refreshToken = jwtUtil.generateRefreshToken(korisnik);

        log.info("Korisnik '{}' se uspješno prijavio.", korisnik.getUsername());

        return buildResponse(token, refreshToken, korisnik);
    }

    @Override
    public LoginResponseDTO refreshToken(String refreshToken) {
        String username = jwtUtil.extractUsername(refreshToken);

        Korisnik korisnik = korisnikRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Korisnik nije pronađen: " + username));

        if (!jwtUtil.validateToken(refreshToken, korisnik)) {
            throw new BusinessException("Refresh token nije validan ili je istekao.");
        }

        String newToken = jwtUtil.generateToken(korisnik);
        String newRefreshToken = jwtUtil.generateRefreshToken(korisnik);

        log.info("Token osvježen za korisnika '{}'.", username);

        return buildResponse(newToken, newRefreshToken, korisnik);
    }

    @Override
    public LoginResponseDTO.KorisnikInfo me(Korisnik korisnik) {
        return toKorisnikInfo(korisnik);
    }

    private LoginResponseDTO buildResponse(String token, String refreshToken, Korisnik korisnik) {
        return new LoginResponseDTO(
                token,
                refreshToken,
                "Bearer",
                jwtProperties.getExpirationMs() / 1000,
                toKorisnikInfo(korisnik)
        );
    }

    private LoginResponseDTO.KorisnikInfo toKorisnikInfo(Korisnik korisnik) {
        Kompanija kompanija = korisnik.getIdKompanije() != null
                ? kompanijaRepository.findById(korisnik.getIdKompanije()).orElse(null)
                : null;

        Poslovnica poslovnica = korisnik.getIdPoslovnice() != null
                ? poslovnicaRepository.findById(korisnik.getIdPoslovnice()).orElse(null)
                : null;

        return new LoginResponseDTO.KorisnikInfo(
                korisnik.getId(),
                korisnik.getUsername(),
                korisnik.getIme(),
                korisnik.getPrezime(),
                korisnik.getEmail(),
                korisnik.getUloga(),
                korisnik.getIdPoslovnice(),
                poslovnica != null ? poslovnica.getNaziv() : null,
                poslovnica != null ? poslovnica.getAdresa() : null,
                poslovnica != null ? poslovnica.getGrad() : null,
                kompanija != null ? kompanija.getNaziv() : null,
                kompanija != null ? kompanija.getAdresa() : null,
                kompanija != null ? kompanija.getGrad() : null,
                toBase64Image(kompanija != null ? kompanija.getLogo() : null),
                korisnik.isAktivan()
        );
    }

    private String toBase64Image(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        String mimeType;
        if (bytes.length >= 4 && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 'P') {
            mimeType = "image/png";
        } else if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8) {
            mimeType = "image/jpeg";
        } else if (bytes.length >= 3 && bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F') {
            mimeType = "image/gif";
        } else {
            mimeType = "image/png";
        }
        return "data:" + mimeType + ";base64," + java.util.Base64.getEncoder().encodeToString(bytes);
    }
}
