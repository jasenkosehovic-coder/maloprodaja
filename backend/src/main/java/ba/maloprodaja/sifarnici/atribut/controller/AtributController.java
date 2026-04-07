package ba.maloprodaja.sifarnici.atribut.controller;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.common.dto.ApiResponse;
import ba.maloprodaja.sifarnici.atribut.dto.ArtikalAtributDTO;
import ba.maloprodaja.sifarnici.atribut.dto.DefinicijaAtributaDTO;
import ba.maloprodaja.sifarnici.atribut.dto.VrijednostAtributaDTO;
import ba.maloprodaja.sifarnici.atribut.service.IAtributService;
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
public class AtributController {

    private final IAtributService atributService;

    // ---- Definicije ----

    @GetMapping("/api/atributi/definicije")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<List<DefinicijaAtributaDTO.ListItemDTO>>> listDefinicije(Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(atributService.listDefinicije(korisnik.getIdKompanije())));
    }

    @PostMapping("/api/atributi/definicije")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<DefinicijaAtributaDTO.ListItemDTO>> createDefinicija(
            @Valid @RequestBody DefinicijaAtributaDTO.CreateDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        DefinicijaAtributaDTO.ListItemDTO created = atributService.createDefinicija(dto, korisnik.getIdKompanije());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PutMapping("/api/atributi/definicije/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<DefinicijaAtributaDTO.ListItemDTO>> updateDefinicija(
            @PathVariable Long id,
            @Valid @RequestBody DefinicijaAtributaDTO.UpdateDTO dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(atributService.updateDefinicija(id, dto)));
    }

    @DeleteMapping("/api/atributi/definicije/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<Void>> deactivateDefinicija(@PathVariable Long id) {
        atributService.deactivateDefinicija(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ---- Vrijednosti ----

    @GetMapping("/api/atributi/definicije/{id}/vrijednosti")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<List<VrijednostAtributaDTO.ListItemDTO>>> listVrijednosti(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(atributService.listVrijednosti(id)));
    }

    @PostMapping("/api/atributi/definicije/{id}/vrijednosti")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<VrijednostAtributaDTO.ListItemDTO>> addVrijednost(
            @PathVariable Long id,
            @Valid @RequestBody VrijednostAtributaDTO.CreateDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        VrijednostAtributaDTO.ListItemDTO created = atributService.addVrijednost(id, dto, korisnik.getIdKompanije());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PutMapping("/api/atributi/vrijednosti/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<VrijednostAtributaDTO.ListItemDTO>> updateVrijednost(
            @PathVariable Long id,
            @Valid @RequestBody VrijednostAtributaDTO.UpdateDTO dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(atributService.updateVrijednost(id, dto)));
    }

    @DeleteMapping("/api/atributi/vrijednosti/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<Void>> deactivateVrijednost(@PathVariable Long id) {
        atributService.deactivateVrijednost(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ---- Artikal atributi ----

    @GetMapping("/api/artikli/{id}/atributi")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<List<ArtikalAtributDTO.ListItemDTO>>> listArtikalAtributi(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(atributService.listArtikalAtributi(id)));
    }

    @PutMapping("/api/artikli/{id}/atributi")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<List<ArtikalAtributDTO.ListItemDTO>>> upsertArtikalAtributi(
            @PathVariable Long id,
            @Valid @RequestBody List<ArtikalAtributDTO.UpsertItemDTO> dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(atributService.upsertArtikalAtributi(id, dto, korisnik.getIdKompanije())));
    }
}
