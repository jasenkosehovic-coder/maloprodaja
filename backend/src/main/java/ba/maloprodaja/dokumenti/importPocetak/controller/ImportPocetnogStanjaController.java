package ba.maloprodaja.dokumenti.importPocetak.controller;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.common.dto.ApiResponse;
import ba.maloprodaja.dokumenti.importPocetak.dto.ImportResultDTO;
import ba.maloprodaja.dokumenti.importPocetak.service.IImportPocetnogStanjaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/dokumenti/import")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ImportPocetnogStanjaController {

    private final IImportPocetnogStanjaService importService;

    @PostMapping("/pocetno-stanje")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ImportResultDTO>> importujPocetnoStanje(
            @RequestParam("file") MultipartFile file,
            Authentication auth
    ) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        ImportResultDTO result = importService.importuj(
                file,
                korisnik.getIdKompanije(),
                korisnik.getIdPoslovnice()
        );
        return ResponseEntity.ok(ApiResponse.ok("Import završen", result));
    }
}
