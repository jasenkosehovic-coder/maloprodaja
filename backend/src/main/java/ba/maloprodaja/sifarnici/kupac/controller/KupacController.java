package ba.maloprodaja.sifarnici.kupac.controller;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.common.dto.ApiResponse;
import ba.maloprodaja.sifarnici.kupac.dto.KupacDTO;
import ba.maloprodaja.sifarnici.kupac.service.IKupacService;
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
@RequestMapping("/api/kupci")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class KupacController {

    private final IKupacService kupacService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'BLAGAJNIK', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<List<KupacDTO.ListItemDTO>>> listAll(Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(kupacService.listAll(korisnik.getIdKompanije())));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'BLAGAJNIK', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<KupacDTO.ListItemDTO>> create(
            @Valid @RequestBody KupacDTO.CreateDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        KupacDTO.ListItemDTO created = kupacService.create(dto, korisnik.getIdKompanije());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'BLAGAJNIK', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<KupacDTO.ListItemDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody KupacDTO.UpdateDTO dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(kupacService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'BLAGAJNIK', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        kupacService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
