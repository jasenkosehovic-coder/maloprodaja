package ba.maloprodaja.auth.controller;

import ba.maloprodaja.auth.dto.KorisnikDTO;
import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.auth.service.IKorisnikService;
import ba.maloprodaja.common.dto.ApiResponse;
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
@RequestMapping("/api/korisnici")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class KorisnikController {

    private final IKorisnikService korisnikService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<KorisnikDTO.KorisnikListItemDTO>>> listAll(Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(korisnikService.listAll(korisnik.getIdKompanije())));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<KorisnikDTO.KorisnikListItemDTO>> create(
            @Valid @RequestBody KorisnikDTO.CreateKorisnikDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        KorisnikDTO.KorisnikListItemDTO created = korisnikService.create(dto, korisnik.getIdKompanije());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<KorisnikDTO.KorisnikListItemDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody KorisnikDTO.UpdateKorisnikDTO dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(korisnikService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        korisnikService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{id}/izbornici")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<KorisnikDTO.KorisnikIzbornikaDTO>>> getIzbornici(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(korisnikService.getIzbornici(id)));
    }

    @PutMapping("/{id}/izbornici")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<KorisnikDTO.KorisnikIzbornikaDTO>>> updateIzbornici(
            @PathVariable Long id,
            @RequestBody List<KorisnikDTO.KorisnikIzbornikaDTO> izbornici
    ) {
        return ResponseEntity.ok(ApiResponse.ok(korisnikService.updateIzbornici(id, izbornici)));
    }
}
