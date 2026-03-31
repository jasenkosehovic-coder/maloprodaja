package ba.maloprodaja.sifarnici.artikalkomp.controller;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.common.dto.ApiResponse;
import ba.maloprodaja.sifarnici.artikalkomp.dto.BarkodDTO;
import ba.maloprodaja.sifarnici.artikalkomp.service.IBarkodService;
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
public class BarkodController {

    private final IBarkodService barkodService;

    @GetMapping("/api/barkodovi")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<List<BarkodDTO.ListItemDTO>>> listByKompanija(Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(barkodService.listByKompanija(korisnik.getIdKompanije())));
    }

    @PostMapping("/api/barkodovi")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<BarkodDTO.ListItemDTO>> create(
            @Valid @RequestBody BarkodDTO.CreateDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        BarkodDTO.ListItemDTO created = barkodService.create(dto, korisnik.getIdKompanije(), dto.idPoslovnice());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PutMapping("/api/barkodovi/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<BarkodDTO.ListItemDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody BarkodDTO.UpdateDTO dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(barkodService.update(id, dto)));
    }

    @DeleteMapping("/api/barkodovi/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        barkodService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
