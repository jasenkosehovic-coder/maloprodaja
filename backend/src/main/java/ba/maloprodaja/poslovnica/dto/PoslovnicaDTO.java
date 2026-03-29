package ba.maloprodaja.poslovnica.dto;

public record PoslovnicaDTO(
        Long id,
        String naziv,
        String adresa,
        String grad,
        Long idKompanije
) {}
