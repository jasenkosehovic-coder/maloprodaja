package ba.maloprodaja.promet.tipdokumenta.controller;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.common.dto.ApiResponse;
import ba.maloprodaja.promet.tipdokumenta.dto.TipDokumentaDTO;
import ba.maloprodaja.promet.tipdokumenta.service.ITipDokumentaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tipovi-dokumenata")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TipDokumentaController {

    private final ITipDokumentaService tipDokumentaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA', 'BLAGAJNIK')")
    public ResponseEntity<ApiResponse<List<TipDokumentaDTO>>> findAll(Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(tipDokumentaService.findAll(korisnik.getIdKompanije())));
    }
}
