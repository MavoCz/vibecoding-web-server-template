package net.voldrich.myhome.backend.auth.internal.service;

import net.voldrich.myhome.backend.auth.AuthUser;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import net.voldrich.myhome.backend.auth.internal.dto.AuthResponse;
import net.voldrich.myhome.backend.auth.internal.dto.LoginRequest;
import net.voldrich.myhome.backend.auth.internal.dto.RegisterRequest;
import net.voldrich.myhome.backend.auth.internal.dto.TokenRefreshRequest;
import net.voldrich.myhome.backend.auth.internal.repository.RefreshTokenRepository;
import net.voldrich.myhome.backend.auth.internal.repository.UserRepository;
import net.voldrich.myhome.backend.auth.internal.security.AuthUserDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                       JwtTokenService jwtTokenService, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        var user = userRepository.create(request.email(), passwordEncoder.encode(request.password()), request.displayName());

        return generateAuthResponse(user.getId(), user.getEmail(), user.getDisplayName());
    }

    public AuthResponse login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        var userDetails = (AuthUserDetails) authentication.getPrincipal();
        return generateAuthResponse(userDetails.userId(), userDetails.email(), userDetails.displayName());
    }

    @Transactional
    public AuthResponse refresh(TokenRefreshRequest request) {
        String tokenHash = jwtTokenService.hashRefreshToken(request.refreshToken());
        var storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        // Revoke old token (rotation)
        refreshTokenRepository.revokeByTokenHash(tokenHash);

        Long userId = storedToken.getUserId();
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return generateAuthResponse(userId, user.getEmail(), user.getDisplayName());
    }

    public void logout(String refreshToken) {
        String tokenHash = jwtTokenService.hashRefreshToken(refreshToken);
        refreshTokenRepository.revokeByTokenHash(tokenHash);
    }

    private AuthResponse generateAuthResponse(Long userId, String email, String displayName) {
        String accessToken = jwtTokenService.generateAccessToken(userId, email, displayName);
        String refreshToken = jwtTokenService.generateRefreshToken();
        String refreshTokenHash = jwtTokenService.hashRefreshToken(refreshToken);

        OffsetDateTime expiresAt = OffsetDateTime.ofInstant(
                Instant.now().plusMillis(jwtTokenService.getRefreshTokenExpirationMs()), ZoneOffset.UTC);
        refreshTokenRepository.create(userId, refreshTokenHash, null, expiresAt);

        return new AuthResponse(
                accessToken,
                refreshToken,
                jwtTokenService.getAccessTokenExpirationMs() / 1000,
                new AuthUser(userId, email, displayName)
        );
    }
}
