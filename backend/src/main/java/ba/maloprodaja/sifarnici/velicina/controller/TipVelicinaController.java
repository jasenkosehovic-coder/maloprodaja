package ba.maloprodaja.sifarnici.velicina.controller;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.common.dto.ApiResponse;
import ba.maloprodaja.sifarnici.velicina.dto.TipVelicinaDTO;
import ba.maloprodaja.sifarnici.velicina.dto.VelicinaDTO;
import ba.maloprodaja.sifarnici.velicina.service.ITipVelicinaService;
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
public class TipVelicinaController {

    private final ITipVelicinaService tipVelicinaService;

    @GetMapping("/api/tipovi-velicina")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<List<TipVelicinaDTO.ListItemDTO>>> listAll(Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(tipVelicinaService.listAll(korisnik.getIdKompanije())));
    }

    @PostMapping("/api/tipovi-velicina")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<TipVelicinaDTO.ListItemDTO>> create(
            @Valid @RequestBody TipVelicinaDTO.CreateDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        TipVelicinaDTO.ListItemDTO created = tipVelicinaService.create(dto, korisnik.getIdKompanije());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PutMapping("/api/tipovi-velicina/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<TipVelicinaDTO.ListItemDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody TipVelicinaDTO.UpdateDTO dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(tipVelicinaService.update(id, dto)));
    }

    @DeleteMapping("/api/tipovi-velicina/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        tipVelicinaService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/api/tipovi-velicina/{id}/velicine")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<List<VelicinaDTO.ListItemDTO>>> listVelicine(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(tipVelicinaService.listVelicine(id)));
    }

    @PostMapping("/api/tipovi-velicina/{id}/velicine")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<VelicinaDTO.ListItemDTO>> addVelicina(
            @PathVariable Long id,
            @Valid @RequestBody VelicinaDTO.CreateDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        VelicinaDTO.ListItemDTO created = tipVelicinaService.addVelicina(id, dto, korisnik.getIdKompanije());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PutMapping("/api/velicine/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<VelicinaDTO.ListItemDTO>> updateVelicina(
            @PathVariable Long id,
            @Valid @RequestBody VelicinaDTO.UpdateDTO dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(tipVelicinaService.updateVelicina(id, dto)));
    }

    @DeleteMapping("/api/velicine/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<Void>> deactivateVelicina(@PathVariable Long id) {
        tipVelicinaService.deactivateVelicina(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
