package com.policene.eventhub.service;

import com.policene.eventhub.dto.auth.RefreshTokenRequestDTO;
import com.policene.eventhub.entity.RefreshToken;
import com.policene.eventhub.entity.User;
import com.policene.eventhub.enums.Role;
import com.policene.eventhub.dto.auth.AuthResponseDTO;
import com.policene.eventhub.dto.auth.LoginRequestDTO;
import com.policene.eventhub.dto.auth.RegisterRequestDTO;
import com.policene.eventhub.exception.EmailAlreadyRegisteredException;
import com.policene.eventhub.repository.UserRepository;
import com.policene.eventhub.security.JwtService;
import com.policene.eventhub.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class AuthService  {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponseDTO register(RegisterRequestDTO request, String ip, String deviceInfo) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyRegisteredException();
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);

        User userSaved = userRepository.save(user);
        String refreshToken = refreshTokenService.generate(userSaved, ip, deviceInfo);
        String token = jwtService.generateToken(new UserDetailsImpl(userSaved));

        return new AuthResponseDTO(token, refreshToken);
    }

    public AuthResponseDTO login(LoginRequestDTO request, String ip, String deviceInfo) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas.");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Credenciais inválidas."));

        String refreshToken = refreshTokenService.generate(user, ip, deviceInfo);
        String token = jwtService.generateToken(new UserDetailsImpl(user));

        return new AuthResponseDTO(token, refreshToken);
    }

    @Transactional
    public AuthResponseDTO refresh (RefreshTokenRequestDTO request, String ip, String deviceInfo) {
        RefreshToken oldRefreshToken = refreshTokenService.getByToken(request.refreshToken());

        if (oldRefreshToken.isUsed()) {
            refreshTokenService.revokeAllByUser(oldRefreshToken.getUser());
            throw new SecurityException("Refresh token já usado.");
        }

        if (oldRefreshToken.isRevoked()) throw new SecurityException("Refresh token já revogado.");
        if (oldRefreshToken.getExpiresAt().isBefore(Instant.now())) throw new SecurityException("Refresh token expirado");

        refreshTokenService.consume(request.refreshToken());

        String newRefreshToken = refreshTokenService.generate(oldRefreshToken.getUser(), ip, deviceInfo);
        String token = jwtService.generateToken(new UserDetailsImpl(oldRefreshToken.getUser()));
        return new AuthResponseDTO(token, newRefreshToken);
    }
}
