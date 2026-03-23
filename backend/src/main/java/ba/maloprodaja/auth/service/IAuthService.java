package ba.maloprodaja.auth.service;

import ba.maloprodaja.auth.dto.LoginRequestDTO;
import ba.maloprodaja.auth.dto.LoginResponseDTO;

public interface IAuthService {

    LoginResponseDTO login(LoginRequestDTO request);

    LoginResponseDTO refreshToken(String refreshToken);
}
