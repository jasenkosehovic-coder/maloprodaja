package ba.maloprodaja.auth.controller;

import ba.maloprodaja.auth.dto.LoginRequestDTO;
import ba.maloprodaja.auth.dto.LoginResponseDTO;
import ba.maloprodaja.auth.dto.RefreshTokenRequestDTO;
import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.auth.service.IAuthService;
import ba.maloprodaja.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request
    ) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> refresh(
            @Valid @RequestBody RefreshTokenRequestDTO request
    ) {
        LoginResponseDTO response = authService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<LoginResponseDTO.KorisnikInfo>> me(Authentication authentication) {
        Korisnik korisnik = (Korisnik) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(authService.me(korisnik)));
    }
}
