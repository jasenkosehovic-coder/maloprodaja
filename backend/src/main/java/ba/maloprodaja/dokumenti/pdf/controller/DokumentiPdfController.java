package ba.maloprodaja.dokumenti.pdf.controller;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.dokumenti.pdf.service.DokumentiPdfService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dokumenti")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DokumentiPdfController {

    private final DokumentiPdfService pdfService;

    @GetMapping("/fakture/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<byte[]> fakturaPdf(@PathVariable Long id, Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        byte[] pdf = pdfService.prometDokumentPdf(id, korisnik.getIdKompanije());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"faktura-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/nivelacije/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<byte[]> nivelacijaPdf(@PathVariable Long id) {
        byte[] pdf = pdfService.nivelacijaPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"nivelacija-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/otpremnice/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<byte[]> otpremnicaPdf(@PathVariable Long id, Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        byte[] pdf = pdfService.prometDokumentPdf(id, korisnik.getIdKompanije());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"otpremnica-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * Shared promet document PDF endpoint — works for all doc types: UF, IF, PD, MSI, MSU.
     * Returns attachment so the browser offers a Save-As dialog.
     */
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'MENADZER', 'KNJIGOVODJA')")
    public ResponseEntity<byte[]> prometDokumentPdf(@PathVariable Long id, Authentication auth) {
        Korisnik korisnik = (Korisnik) auth.getPrincipal();
        byte[] pdf = pdfService.prometDokumentPdf(id, korisnik.getIdKompanije());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"dokument-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
