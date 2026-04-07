package ba.maloprodaja.promet.stavka.controller;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.common.dto.ApiResponse;
import ba.maloprodaja.promet.dokument.dto.DokumentDTO;
import ba.maloprodaja.promet.dokument.service.IDokumentService;
import ba.maloprodaja.promet.stavka.dto.StavkaDTO;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stavke-dokumenata")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class StavkaController {

    private final IDokumentService dokumentService;

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<DokumentDTO.DokumentResponseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody StavkaDTO.UpdateStavkaDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(dokumentService.updateStavka(id, dto, korisnik.getIdKompanije())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id, Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        dokumentService.deleteStavka(id, korisnik.getIdKompanije());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
