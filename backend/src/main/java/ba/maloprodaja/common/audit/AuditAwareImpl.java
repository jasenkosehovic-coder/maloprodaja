package ba.maloprodaja.common.audit;

import ba.maloprodaja.auth.entity.Korisnik;
import ba.maloprodaja.common.security.UserPrincipal;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditAwareImpl")
public class AuditAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof Korisnik korisnik) {
            return Optional.of(korisnik.getId());
        }

        if (authentication.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal.getId());
        }

        return Optional.empty();
    }
}
