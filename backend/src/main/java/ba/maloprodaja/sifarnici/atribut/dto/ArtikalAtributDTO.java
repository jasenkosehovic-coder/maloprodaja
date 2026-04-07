package ba.maloprodaja.sifarnici.atribut.dto;

import jakarta.validation.constraints.NotNull;

public class ArtikalAtributDTO {

    public record ListItemDTO(
            Long id,
            Long idDefinicije,
            String nazivDefinicije,
            Long idVrijednosti,
            String vrijednost
    ) {}

    public record UpsertItemDTO(
            @NotNull(message = "ID definicije je obavezan")
            Long idDefinicije,

            @NotNull(message = "ID vrijednosti je obavezan")
            Long idVrijednosti
    ) {}
}
