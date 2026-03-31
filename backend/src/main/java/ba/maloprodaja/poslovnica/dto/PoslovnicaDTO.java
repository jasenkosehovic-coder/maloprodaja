package ba.maloprodaja.poslovnica.dto;

public record PoslovnicaDTO(
        Long id,
        String naziv,
        String adresa,
        String grad,
        String telefon,
        String email,
        String pib,
        Long idKompanije
) {}
