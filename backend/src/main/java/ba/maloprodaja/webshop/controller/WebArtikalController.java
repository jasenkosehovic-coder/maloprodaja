package ba.maloprodaja.webshop.controller;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.common.dto.ApiResponse;
import ba.maloprodaja.webshop.dto.WebArtikalDTO;
import ba.maloprodaja.webshop.service.IWebArtikalService;
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
@RequestMapping("/api/web-artikli")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class WebArtikalController {

    private final IWebArtikalService webArtikalService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<List<WebArtikalDTO.ListItemDTO>>> list(Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(webArtikalService.list(korisnik.getIdKompanije())));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<WebArtikalDTO.ListItemDTO>> upsert(
            @Valid @RequestBody WebArtikalDTO.CreateDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        WebArtikalDTO.ListItemDTO result = webArtikalService.upsert(dto, korisnik.getIdKompanije());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<WebArtikalDTO.ListItemDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody WebArtikalDTO.UpdateDTO dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok(webArtikalService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        webArtikalService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
