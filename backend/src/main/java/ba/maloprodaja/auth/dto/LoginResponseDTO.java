package ba.maloprodaja.auth.dto;

import ba.maloprodaja.auth.entity.KorisnikUloga;

public record LoginResponseDTO(
        String token,
        String refreshToken,
        KorisnikInfo korisnik
) {
    public record KorisnikInfo(
            Long id,
            String username,
            String ime,
            String prezime,
            KorisnikUloga uloga
    ) {}
}
