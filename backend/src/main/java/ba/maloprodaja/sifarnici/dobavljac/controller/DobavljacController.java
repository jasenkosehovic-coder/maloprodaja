package ba.maloprodaja.sifarnici.dobavljac.controller;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.common.dto.ApiResponse;
import ba.maloprodaja.sifarnici.dobavljac.dto.DobavljacDTO;
import ba.maloprodaja.sifarnici.dobavljac.service.IDobavljacService;
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
@RequestMapping("/api/dobavljaci")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DobavljacController {

    private final IDobavljacService dobavljacService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<List<DobavljacDTO.ListItemDTO>>> listAll(Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(dobavljacService.listAll(korisnik.getIdKompanije())));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<DobavljacDTO.ListItemDTO>> create(
            @Valid @RequestBody DobavljacDTO.CreateDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        DobavljacDTO.ListItemDTO created = dobavljacService.create(dto, korisnik.getIdKompanije());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<DobavljacDTO.ListItemDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody DobavljacDTO.UpdateDTO dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(dobavljacService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        dobavljacService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
