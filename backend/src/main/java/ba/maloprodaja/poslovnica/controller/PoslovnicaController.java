package ba.maloprodaja.poslovnica.controller;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.common.dto.ApiResponse;
import ba.maloprodaja.poslovnica.dto.PoslovnicaDTO;
import ba.maloprodaja.poslovnica.service.IPoslovnicaService;
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
@RequestMapping("/api/poslovnice")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class PoslovnicaController {

    private final IPoslovnicaService poslovnicaService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PoslovnicaDTO>>> listAll(Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(poslovnicaService.listByKompanija(korisnik.getIdKompanije())));
    }
}
