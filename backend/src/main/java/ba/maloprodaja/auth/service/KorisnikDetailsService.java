package ba.maloprodaja.auth.service;

import ba.maloprodaja.auth.repository.KorisnikRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KorisnikDetailsService implements UserDetailsService {

    private final KorisnikRepository korisnikRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return korisnikRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Korisnik nije pronađen: " + username));
    }
}
