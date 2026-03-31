package ba.maloprodaja.sifarnici.proizvodjac.controller;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.common.dto.ApiResponse;
import ba.maloprodaja.sifarnici.proizvodjac.dto.ProizvodjacDTO;
import ba.maloprodaja.sifarnici.proizvodjac.service.IProizvodjacService;
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
@RequestMapping("/api/proizvodjaci")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ProizvodjacController {

    private final IProizvodjacService proizvodjacService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<List<ProizvodjacDTO.ListItemDTO>>> listAll(Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(proizvodjacService.listAll(korisnik.getIdKompanije())));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<ProizvodjacDTO.ListItemDTO>> create(
            @Valid @RequestBody ProizvodjacDTO.CreateDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        ProizvodjacDTO.ListItemDTO created = proizvodjacService.create(dto, korisnik.getIdKompanije());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<ProizvodjacDTO.ListItemDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProizvodjacDTO.UpdateDTO dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(proizvodjacService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        proizvodjacService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
