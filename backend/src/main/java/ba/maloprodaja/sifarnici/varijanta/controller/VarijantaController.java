package ba.maloprodaja.sifarnici.varijanta.controller;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.common.dto.ApiResponse;
import ba.maloprodaja.sifarnici.varijanta.dto.VarijantaDTO;
import ba.maloprodaja.sifarnici.varijanta.service.IVarijantaService;
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
public class VarijantaController {

    private final IVarijantaService varijantaService;

    @GetMapping("/api/artikli/{idArtikla}/varijante")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<List<VarijantaDTO.ListItemDTO>>> listByArtikl(
            @PathVariable Long idArtikla,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                varijantaService.listByArtikl(idArtikla, korisnik.getIdKompanije())));
    }

    @PostMapping("/api/artikli/{idArtikla}/varijante")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<VarijantaDTO.ListItemDTO>> create(
            @PathVariable Long idArtikla,
            @Valid @RequestBody VarijantaDTO.CreateDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        VarijantaDTO.ListItemDTO created = varijantaService.create(idArtikla, dto, korisnik.getIdKompanije());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PutMapping("/api/varijante/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<VarijantaDTO.ListItemDTO>> updateAktivan(
            @PathVariable Long id,
            @Valid @RequestBody VarijantaDTO.UpdateAktivanDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                varijantaService.updateAktivan(id, dto, korisnik.getIdKompanije())));
    }

    @GetMapping("/api/artikli/{idArtikla}/stanje")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA', 'BLAGAJNIK')")
    public ResponseEntity<ApiResponse<List<VarijantaDTO.StanjeVarijanteDTO>>> stanjeByArtikl(
            @PathVariable Long idArtikla,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                varijantaService.stanjeByArtikl(idArtikla, korisnik.getIdKompanije())));
    }

    @GetMapping("/api/varijante/{idVarijante}/stanje")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA', 'BLAGAJNIK')")
    public ResponseEntity<ApiResponse<List<VarijantaDTO.StanjePoslovniceDTO>>> stanjeByVarijanta(
            @PathVariable Long idVarijante,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                varijantaService.stanjeByVarijanta(idVarijante, korisnik.getIdKompanije())));
    }

    @PutMapping("/api/varijante-poslovnica/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<VarijantaDTO.StanjePoslovniceDTO>> updateStanje(
            @PathVariable Long id,
            @Valid @RequestBody VarijantaDTO.UpdateStanjeDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                varijantaService.updateStanje(id, dto, korisnik.getIdKompanije())));
    }

    @PutMapping("/api/varijante-poslovnica/{id}/zalihe")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<VarijantaDTO.StanjePoslovniceDTO>> updateZalihe(
            @PathVariable Long id,
            @RequestBody VarijantaDTO.UpdateZaliheDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                varijantaService.updateZalihe(id, dto, korisnik.getIdKompanije())));
    }

    @GetMapping("/api/varijante-poslovnica")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<List<VarijantaDTO.ZalihaListItemDTO>>> listZaliheByPoslovnica(
            @RequestParam Long idPoslovnice,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                varijantaService.listZaliheByPoslovnica(idPoslovnice, korisnik.getIdKompanije())));
    }
}
