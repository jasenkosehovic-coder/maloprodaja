package ba.maloprodaja.promet.tipdokumenta.dto;

public record TipDokumentaDTO(
        Long id,
        String kod,
        String naziv,
        short smjerKolicine
) {}
