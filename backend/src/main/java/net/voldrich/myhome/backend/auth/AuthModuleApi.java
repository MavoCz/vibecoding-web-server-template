package net.voldrich.myhome.backend.auth;

import java.util.Optional;

public interface AuthModuleApi {

    Optional<AuthUser> getCurrentUser();
}
