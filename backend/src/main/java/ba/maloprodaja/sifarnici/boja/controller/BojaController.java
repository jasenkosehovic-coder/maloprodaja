package ba.maloprodaja.sifarnici.boja.controller;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.common.dto.ApiResponse;
import ba.maloprodaja.sifarnici.boja.dto.BojaDTO;
import ba.maloprodaja.sifarnici.boja.service.IBojaService;
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
public class BojaController {

    private final IBojaService bojaService;

    @GetMapping("/api/boje")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<List<BojaDTO.ListItemDTO>>> listAll(Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(bojaService.listAll(korisnik.getIdKompanije())));
    }

    @GetMapping("/api/boje/aktivne")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<List<BojaDTO.ListItemDTO>>> listAktivne(Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(bojaService.listAktivne(korisnik.getIdKompanije())));
    }

    @PostMapping("/api/boje")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<BojaDTO.ListItemDTO>> create(
            @Valid @RequestBody BojaDTO.CreateDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        BojaDTO.ListItemDTO created = bojaService.create(dto, korisnik.getIdKompanije());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created));
    }

    @PutMapping("/api/boje/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<BojaDTO.ListItemDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody BojaDTO.UpdateDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(bojaService.update(id, dto, korisnik.getIdKompanije())));
    }

    @DeleteMapping("/api/boje/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        bojaService.delete(id, korisnik.getIdKompanije());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
