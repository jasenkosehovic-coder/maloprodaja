package ba.maloprodaja.dokumenti.nivelacija.controller;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.common.dto.ApiResponse;
import ba.maloprodaja.dokumenti.nivelacija.dto.NivelacijaDTO;
import ba.maloprodaja.dokumenti.nivelacija.service.INivelacijaService;
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
@RequestMapping("/api/dokumenti/nivelacije")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class NivelacijaController {

    private final INivelacijaService nivelacijaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<List<NivelacijaDTO.ListItemDTO>>> listAll(Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(
                nivelacijaService.listAll(korisnik.getIdKompanije(), korisnik.getIdPoslovnice())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<NivelacijaDTO.DetailDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(nivelacijaService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<NivelacijaDTO.DetailDTO>> kreirajRucnu(
            @Valid @RequestBody NivelacijaDTO.CreateRucnaDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        NivelacijaDTO.DetailDTO created = nivelacijaService.kreirajRucnu(dto, korisnik.getIdKompanije(), korisnik.getIdPoslovnice());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }
}
