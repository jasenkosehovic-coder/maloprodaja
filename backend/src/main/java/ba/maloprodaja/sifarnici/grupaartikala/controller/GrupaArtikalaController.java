package ba.maloprodaja.sifarnici.grupaartikala.controller;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.common.dto.ApiResponse;
import ba.maloprodaja.sifarnici.grupaartikala.dto.GrupaArtikalaDTO;
import ba.maloprodaja.sifarnici.grupaartikala.service.IGrupaArtikalaService;
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
@RequestMapping("/api/grupe-artikala")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class GrupaArtikalaController {

    private final IGrupaArtikalaService grupaArtikalaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<List<GrupaArtikalaDTO.ListItemDTO>>> listAll(Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(grupaArtikalaService.listAll(korisnik.getIdKompanije())));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<GrupaArtikalaDTO.ListItemDTO>> create(
            @Valid @RequestBody GrupaArtikalaDTO.CreateDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        GrupaArtikalaDTO.ListItemDTO created = grupaArtikalaService.create(dto, korisnik.getIdKompanije());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<GrupaArtikalaDTO.ListItemDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody GrupaArtikalaDTO.UpdateDTO dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(grupaArtikalaService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        grupaArtikalaService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
