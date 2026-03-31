package ba.maloprodaja.sifarnici.proizvodjac.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProizvodjacDTO {

    public record ListItemDTO(
            Long id,
            String naziv,
            String drzava,
            String kontaktOsoba,
            String telefon,
            String email,
            boolean aktivan
    ) {}

    public record CreateDTO(
            @NotBlank(message = "Naziv je obavezan")
            @Size(max = 200, message = "Naziv ne smije biti duži od 200 znakova")
            String naziv,

            @Size(max = 100, message = "Država ne smije biti duža od 100 znakova")
            String drzava,

            @Size(max = 150, message = "Kontakt osoba ne smije biti duža od 150 znakova")
            String kontaktOsoba,

            @Size(max = 50, message = "Telefon ne smije biti duži od 50 znakova")
            String telefon,

            @Email(message = "Email nije ispravan")
            @Size(max = 150, message = "Email ne smije biti duži od 150 znakova")
            String email
    ) {}

    public record UpdateDTO(
            @NotBlank(message = "Naziv je obavezan")
            @Size(max = 200, message = "Naziv ne smije biti duži od 200 znakova")
            String naziv,

            @Size(max = 100, message = "Država ne smije biti duža od 100 znakova")
            String drzava,

            @Size(max = 150, message = "Kontakt osoba ne smije biti duža od 150 znakova")
            String kontaktOsoba,

            @Size(max = 50, message = "Telefon ne smije biti duži od 50 znakova")
            String telefon,

            @Email(message = "Email nije ispravan")
            @Size(max = 150, message = "Email ne smije biti duži od 150 znakova")
            String email,

            Boolean aktivan
    ) {}
}
