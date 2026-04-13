package ba.maloprodaja.sifarnici.artikalposl.controller;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.common.dto.ApiResponse;
import ba.maloprodaja.sifarnici.artikalposl.dto.ArtikalPoslovnicaDTO;
import ba.maloprodaja.sifarnici.artikalposl.service.IArtikalPoslovnicaService;
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
public class ArtikalPoslovnicaController {

    private final IArtikalPoslovnicaService artikalPoslovnicaService;

    @GetMapping("/api/artikli-poslovnice/kompanija")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<ArtikalPoslovnicaDTO.ListItemDTO>>> listByKompanija(
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(artikalPoslovnicaService.listByKompanija(korisnik.getIdKompanije())));
    }

    @GetMapping("/api/artikli-poslovnice")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<List<ArtikalPoslovnicaDTO.ListItemDTO>>> listByPoslovnica(
            @RequestParam Long idPoslovnice
    ) {
        return ResponseEntity.ok(ApiResponse.ok(artikalPoslovnicaService.listByPoslovnica(idPoslovnice)));
    }

    @GetMapping("/api/artikli-poslovnice/artikal/{idArtikla}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<List<ArtikalPoslovnicaDTO.ListItemDTO>>> listByArtikalKompanija(
            @PathVariable Long idArtikla
    ) {
        return ResponseEntity.ok(ApiResponse.ok(artikalPoslovnicaService.listByArtikalKompanija(idArtikla)));
    }

    @PostMapping("/api/artikli-poslovnice")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<ArtikalPoslovnicaDTO.ListItemDTO>> create(
            @Valid @RequestBody ArtikalPoslovnicaDTO.CreateDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        ArtikalPoslovnicaDTO.ListItemDTO created = artikalPoslovnicaService.create(dto, korisnik.getIdKompanije());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PutMapping("/api/artikli-poslovnice/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<ArtikalPoslovnicaDTO.ListItemDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody ArtikalPoslovnicaDTO.UpdateDTO dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(artikalPoslovnicaService.update(id, dto)));
    }

    @DeleteMapping("/api/artikli-poslovnice/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        artikalPoslovnicaService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PatchMapping("/api/artikli-poslovnice/batch-mpc")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<Void>> batchUpdateMpc(
            @Valid @RequestBody List<ArtikalPoslovnicaDTO.BatchMpcUpdateDTO> updates
    ) {
        artikalPoslovnicaService.batchUpdateMpc(updates);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PatchMapping("/api/artikli-poslovnice/batch-popust")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<Void>> batchUpdatePopust(
            @Valid @RequestBody List<ArtikalPoslovnicaDTO.BatchPopustUpdateDTO> updates
    ) {
        artikalPoslovnicaService.batchUpdatePopust(updates);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
