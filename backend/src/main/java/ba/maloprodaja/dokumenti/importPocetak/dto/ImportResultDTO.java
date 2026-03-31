package ba.maloprodaja.dokumenti.importPocetak.dto;

import java.util.List;

public record ImportResultDTO(
        int ukupnoRedova,
        int uspjesnoUvezeno,
        int preskoceno,
        List<String> greske
) {}
