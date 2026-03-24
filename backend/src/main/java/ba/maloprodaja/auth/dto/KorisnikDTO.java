package ba.maloprodaja.auth.dto;

import ba.maloprodaja.auth.entity.KorisnikUloga;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class KorisnikDTO {

    public record KorisnikListItemDTO(
            Long id,
            String username,
            String ime,
            String prezime,
            String email,
            KorisnikUloga uloga,
            Long poslovnicaId,
            boolean aktivan
    ) {}

    public record CreateKorisnikDTO(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Size(min = 6) String password,
            @Size(max = 100) String ime,
            @Size(max = 100) String prezime,
            @Email @Size(max = 150) String email,
            @NotNull KorisnikUloga uloga,
            Long poslovnicaId
    ) {}

    public record UpdateKorisnikDTO(
            @Size(max = 100) String ime,
            @Size(max = 100) String prezime,
            @Email @Size(max = 150) String email,
            KorisnikUloga uloga,
            Long poslovnicaId,
            Boolean aktivan
    ) {}

    public record KorisnikIzbornikaDTO(
            String izbornikKljuc,
            boolean aktivan
    ) {}
}
