package ba.maloprodaja.sifarnici.slika.controller;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.common.dto.ApiResponse;
import ba.maloprodaja.sifarnici.slika.dto.SlikaArtiklaDTO;
import ba.maloprodaja.sifarnici.slika.service.ISlikaArtiklaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SlikaArtiklaController {

    private final ISlikaArtiklaService slikaArtiklaService;

    @GetMapping("/api/artikli/{id}/slike")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<List<SlikaArtiklaDTO.ListItemDTO>>> listSlike(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(slikaArtiklaService.listSlike(id)));
    }

    @PostMapping(value = "/api/artikli/{id}/slike", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<SlikaArtiklaDTO.ListItemDTO>> uploadSlika(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        SlikaArtiklaDTO.ListItemDTO created = slikaArtiklaService.uploadSlika(id, file, korisnik.getIdKompanije());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PutMapping("/api/slike/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<SlikaArtiklaDTO.ListItemDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody SlikaArtiklaDTO.UpdateDTO dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(slikaArtiklaService.update(id, dto)));
    }

    @DeleteMapping("/api/slike/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        slikaArtiklaService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
