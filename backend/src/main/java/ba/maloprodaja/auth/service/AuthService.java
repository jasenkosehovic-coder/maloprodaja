package ba.maloprodaja.auth.service;

import ba.maloprodaja.auth.dto.LoginRequestDTO;
import ba.maloprodaja.auth.dto.LoginResponseDTO;
import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.auth.repository.KorisnikRepository;
import ba.maloprodaja.common.exception.BusinessException;
import ba.maloprodaja.common.security.JwtProperties;
import ba.maloprodaja.common.security.JwtUtil;
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
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        Korisnik korisnik = (Korisnik) authentication.getPrincipal();

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
        return new LoginResponseDTO.KorisnikInfo(
                korisnik.getId(),
                korisnik.getUsername(),
                korisnik.getIme(),
                korisnik.getPrezime(),
                korisnik.getEmail(),
                korisnik.getUloga(),
                korisnik.getIdPoslovnice(),
                null,
                korisnik.isAktivan()
        );
    }
}
