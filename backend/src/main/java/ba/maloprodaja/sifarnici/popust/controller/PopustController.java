package ba.maloprodaja.sifarnici.popust.controller;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.common.dto.ApiResponse;
import ba.maloprodaja.sifarnici.popust.dto.PopustDTO;
import ba.maloprodaja.sifarnici.popust.service.IPopustService;
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
@RequestMapping("/api/popusti")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class PopustController {

    private final IPopustService popustService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<List<PopustDTO.ListItemDTO>>> listAll(Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(popustService.listAll(korisnik.getIdKompanije())));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<PopustDTO.ListItemDTO>> create(
            @Valid @RequestBody PopustDTO.CreateDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        PopustDTO.ListItemDTO created = popustService.create(dto, korisnik.getIdKompanije(), korisnik.getIdPoslovnice());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<PopustDTO.ListItemDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody PopustDTO.UpdateDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(popustService.update(id, dto, korisnik.getIdPoslovnice())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        popustService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
