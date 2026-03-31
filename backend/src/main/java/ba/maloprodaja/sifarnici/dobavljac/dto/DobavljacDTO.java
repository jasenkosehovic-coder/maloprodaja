package ba.maloprodaja.sifarnici.dobavljac.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DobavljacDTO {

    public record ListItemDTO(
            Long id,
            String naziv,
            String adresa,
            String grad,
            String telefon,
            String email,
            String pib,
            boolean aktivan
    ) {}

    public record CreateDTO(
            @NotBlank(message = "Naziv je obavezan")
            @Size(max = 200, message = "Naziv ne smije biti duži od 200 znakova")
            String naziv,

            @Size(max = 300, message = "Adresa ne smije biti duža od 300 znakova")
            String adresa,

            @Size(max = 100, message = "Grad ne smije biti duži od 100 znakova")
            String grad,

            @Size(max = 50, message = "Telefon ne smije biti duži od 50 znakova")
            String telefon,

            @Email(message = "Email nije ispravan")
            @Size(max = 150, message = "Email ne smije biti duži od 150 znakova")
            String email,

            @Size(max = 50, message = "PIB ne smije biti duži od 50 znakova")
            String pib
    ) {}

    public record UpdateDTO(
            @NotBlank(message = "Naziv je obavezan")
            @Size(max = 200, message = "Naziv ne smije biti duži od 200 znakova")
            String naziv,

            @Size(max = 300, message = "Adresa ne smije biti duža od 300 znakova")
            String adresa,

            @Size(max = 100, message = "Grad ne smije biti duži od 100 znakova")
            String grad,

            @Size(max = 50, message = "Telefon ne smije biti duži od 50 znakova")
            String telefon,

            @Email(message = "Email nije ispravan")
            @Size(max = 150, message = "Email ne smije biti duži od 150 znakova")
            String email,

            @Size(max = 50, message = "PIB ne smije biti duži od 50 znakova")
            String pib,

            Boolean aktivan
    ) {}
}
