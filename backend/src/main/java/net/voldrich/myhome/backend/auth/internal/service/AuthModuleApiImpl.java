package net.voldrich.myhome.backend.auth.internal.service;

import net.voldrich.myhome.backend.auth.AuthModuleApi;
import net.voldrich.myhome.backend.auth.AuthUser;
import net.voldrich.myhome.backend.auth.internal.security.AuthUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthModuleApiImpl implements AuthModuleApi {

    @Override
    public Optional<AuthUser> getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUserDetails user)) {
            return Optional.empty();
        }
        return Optional.of(new AuthUser(user.userId(), user.email(), user.displayName()));
    }
}
