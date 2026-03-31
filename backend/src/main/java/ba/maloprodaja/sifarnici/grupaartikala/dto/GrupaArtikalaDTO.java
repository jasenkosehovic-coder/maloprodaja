package ba.maloprodaja.sifarnici.grupaartikala.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GrupaArtikalaDTO {

    public record ListItemDTO(
            Long id,
            String naziv,
            String opis,
            boolean aktivan,
            Long idRoditeljskeGrupe,
            String nazivRoditeljskeGrupe
    ) {}

    public record CreateDTO(
            @NotBlank(message = "Naziv je obavezan")
            @Size(max = 200, message = "Naziv ne smije biti duži od 200 znakova")
            String naziv,

            @Size(max = 500, message = "Opis ne smije biti duži od 500 znakova")
            String opis,

            Long idRoditeljskeGrupe
    ) {}

    public record UpdateDTO(
            @NotBlank(message = "Naziv je obavezan")
            @Size(max = 200, message = "Naziv ne smije biti duži od 200 znakova")
            String naziv,

            @Size(max = 500, message = "Opis ne smije biti duži od 500 znakova")
            String opis,

            Long idRoditeljskeGrupe,

            Boolean aktivan
    ) {}
}
