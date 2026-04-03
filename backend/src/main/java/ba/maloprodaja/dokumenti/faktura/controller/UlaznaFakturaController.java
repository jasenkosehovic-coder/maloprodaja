package ba.maloprodaja.dokumenti.faktura.controller;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.common.dto.ApiResponse;
import ba.maloprodaja.dokumenti.faktura.dto.UlaznaFakturaDTO;
import ba.maloprodaja.dokumenti.faktura.service.IUlaznaFakturaService;
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
@RequestMapping("/api/dokumenti/fakture")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UlaznaFakturaController {

    private final IUlaznaFakturaService fakturaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<List<UlaznaFakturaDTO.ListItemDTO>>> listAll(
            Authentication auth,
            @RequestParam(required = false) Integer godina) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                fakturaService.listAll(korisnik.getIdKompanije(), korisnik.getIdPoslovnice(), godina)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<UlaznaFakturaDTO.DetailDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(fakturaService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<UlaznaFakturaDTO.DetailDTO>> create(
            @Valid @RequestBody UlaznaFakturaDTO.CreateDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        UlaznaFakturaDTO.DetailDTO created = fakturaService.create(dto, korisnik.getIdKompanije(), korisnik.getIdPoslovnice());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<UlaznaFakturaDTO.DetailDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody UlaznaFakturaDTO.UpdateDTO dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(fakturaService.update(id, dto)));
    }

    @PostMapping("/{id}/stavke")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<UlaznaFakturaDTO.DetailDTO>> addStavka(
            @PathVariable Long id,
            @Valid @RequestBody UlaznaFakturaDTO.AddStavkaDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(fakturaService.addStavka(id, dto)));
    }

    @DeleteMapping("/{id}/stavke/{stavkaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<UlaznaFakturaDTO.DetailDTO>> removeStavka(
            @PathVariable Long id,
            @PathVariable Long stavkaId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(fakturaService.removeStavka(id, stavkaId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        fakturaService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{id}/potvrdi")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<Void>> potvrdi(@PathVariable Long id) {
        fakturaService.potvrdi(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{id}/storno")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<Void>> storno(@PathVariable Long id) {
        fakturaService.storno(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
