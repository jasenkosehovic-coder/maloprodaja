package ba.maloprodaja.sifarnici.artikalkomp.controller;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.common.dto.ApiResponse;
import ba.maloprodaja.sifarnici.artikalkomp.dto.ArtikalKompDTO;
import ba.maloprodaja.sifarnici.artikalkomp.service.IArtikalKompService;
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
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ArtikalKompController {

    private final IArtikalKompService artikalKompService;

    @GetMapping("/api/artikli")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<List<ArtikalKompDTO.ListItemDTO>>> listAll(Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(artikalKompService.listAll(korisnik.getIdKompanije())));
    }

    @PostMapping("/api/artikli")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<ArtikalKompDTO.ListItemDTO>> create(
            @Valid @RequestBody ArtikalKompDTO.CreateDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        ArtikalKompDTO.ListItemDTO created = artikalKompService.create(dto, korisnik.getIdKompanije());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PutMapping("/api/artikli/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<ArtikalKompDTO.ListItemDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody ArtikalKompDTO.UpdateDTO dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(artikalKompService.update(id, dto)));
    }

    @DeleteMapping("/api/artikli/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        artikalKompService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

}
