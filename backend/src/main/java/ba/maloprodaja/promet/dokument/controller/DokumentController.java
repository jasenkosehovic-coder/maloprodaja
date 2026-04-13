package ba.maloprodaja.promet.dokument.controller;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.common.dto.ApiResponse;
import ba.maloprodaja.common.dto.PageResponseDTO;
import ba.maloprodaja.promet.dokument.dto.DokumentDTO;
import ba.maloprodaja.promet.dokument.service.IDokumentService;
import ba.maloprodaja.promet.stavka.dto.StavkaDTO;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dokumenti")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DokumentController {

    private final IDokumentService dokumentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<PageResponseDTO<DokumentDTO.DokumentListItemDTO>>> findAll(
            Authentication auth,
            @RequestParam(required = false) String tipKod,
            @RequestParam(required = false) Long poslovnicaId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datumOd,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datumDo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(
                dokumentService.findAll(korisnik.getIdKompanije(), tipKod, poslovnicaId,
                        status, datumOd, datumDo, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<DokumentDTO.DokumentResponseDTO>> findById(
            @PathVariable Long id, Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(dokumentService.findById(id, korisnik.getIdKompanije())));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<DokumentDTO.DokumentResponseDTO>> create(
            @Valid @RequestBody DokumentDTO.CreateDokumentDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(dokumentService.create(dto, korisnik.getIdKompanije())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<DokumentDTO.DokumentResponseDTO>> update(
            @PathVariable Long id,
            @Valid @RequestBody DokumentDTO.UpdateDokumentDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(dokumentService.update(id, dto, korisnik.getIdKompanije())));
    }

    @PostMapping("/{id}/potvrdi")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<DokumentDTO.DokumentResponseDTO>> potvrdi(
            @PathVariable Long id, Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(dokumentService.potvrdi(id, korisnik.getIdKompanije())));
    }

    @PostMapping("/{id}/storniraj")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<DokumentDTO.DokumentResponseDTO>> storniraj(
            @PathVariable Long id, Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(dokumentService.storniraj(id, korisnik.getIdKompanije())));
    }

    @PostMapping("/{id}/stavke")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<ApiResponse<DokumentDTO.DokumentResponseDTO>> addStavka(
            @PathVariable Long id,
            @Valid @RequestBody StavkaDTO.CreateStavkaDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(dokumentService.addStavka(id, dto, korisnik.getIdKompanije())));
    }

    @PostMapping("/medjuskladisnica")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER')")
    public ResponseEntity<ApiResponse<Void>> kreirajMedjuskladisnicu(
            @Valid @RequestBody DokumentDTO.CreateMedjuskladisnicaDTO dto,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        dokumentService.kreirajMedjuskladisnicu(dto, korisnik.getIdKompanije());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Međuskladišnica kreirana i potvrđena."));
    }
}
