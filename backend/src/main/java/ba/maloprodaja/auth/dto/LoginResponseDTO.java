package ba.maloprodaja.auth.dto;

import ba.maloprodaja.auth.entity.KorisnikUloga;

public record LoginResponseDTO(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        KorisnikInfo korisnik
) {
    public record KorisnikInfo(
            Long id,
            String username,
            String ime,
            String prezime,
            String email,
            KorisnikUloga uloga,
            Long poslovnicaId,
            String poslovnicaNaziv,
            String poslovnicaAdresa,
            String poslovnicaGrad,
            String kompanijaNaziv,
            String kompanijaAdresa,
            String kompanijaGrad,
            String kompanijaLogo,
            boolean aktivan
    ) {}
}
