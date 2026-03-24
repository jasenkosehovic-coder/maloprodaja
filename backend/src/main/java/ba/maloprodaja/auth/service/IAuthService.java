package ba.maloprodaja.auth.service;

import ba.maloprodaja.auth.dto.LoginRequestDTO;
import ba.maloprodaja.auth.dto.LoginResponseDTO;
import ba.maloprodaja.auth.entity.Korisnik;

public interface IAuthService {

    LoginResponseDTO login(LoginRequestDTO request);

    LoginResponseDTO refreshToken(String refreshToken);

    LoginResponseDTO.KorisnikInfo me(Korisnik korisnik);
}
