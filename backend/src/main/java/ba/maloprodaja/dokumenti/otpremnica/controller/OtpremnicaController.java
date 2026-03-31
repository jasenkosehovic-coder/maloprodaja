package ba.maloprodaja.dokumenti.otpremnica.controller;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.common.dto.ApiResponse;
import ba.maloprodaja.dokumenti.otpremnica.dto.OtpremnicaDTO;
import ba.maloprodaja.dokumenti.otpremnica.service.IOtpremnicaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dokumenti/otpremnice")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class OtpremnicaController {

    private final IOtpremnicaService otpremnicaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<List<OtpremnicaDTO.ListItemDTO>>> listAll(Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                otpremnicaService.listAll(korisnik.getIdKompanije(), korisnik.getIdPoslovnice())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<OtpremnicaDTO.DetailDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(otpremnicaService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<OtpremnicaDTO.DetailDTO>> create(
            @Valid @RequestBody OtpremnicaDTO.CreateDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        OtpremnicaDTO.DetailDTO created = otpremnicaService.create(dto, korisnik.getIdKompanije(), korisnik.getIdPoslovnice());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PostMapping("/{id}/posalji")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<Void>> posalji(@PathVariable Long id) {
        otpremnicaService.posalji(id);
        return ResponseEntity.ok(ApiResponse.ok("Otpremnica uspješno poslana."));
    }

    @PostMapping("/{id}/potvrdi-prijem")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<Void>> potvrdiPrijem(
            @PathVariable Long id,
            @RequestBody(required = false) OtpremnicaDTO.PotvrdiPrijemDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        otpremnicaService.potvrdiPrijem(id, dto, korisnik.getIdPoslovnice());
        return ResponseEntity.ok(ApiResponse.ok("Prijem otpremnice uspješno potvrđen."));
    }

    @PostMapping("/{id}/storno")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<Void>> storno(@PathVariable Long id, Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        otpremnicaService.storno(id, korisnik.getIdKompanije(), korisnik.getIdPoslovnice());
        return ResponseEntity.ok(ApiResponse.ok("Otpremnica uspješno stornirana."));
    }
}
