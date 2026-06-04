package com.policene.eventhub.service;

import com.policene.eventhub.entity.RefreshToken;
import com.policene.eventhub.entity.User;
import com.policene.eventhub.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${refresh.token.expiration.days}")
    private int refreshTokenExpirationDays;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    private String hashToken (String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao fazer hash do token", e);
        }
    }

    private LocalDateTime getExpirationDate () {
        return LocalDateTime.now().plusDays(refreshTokenExpirationDays);
    }

    public RefreshToken getByToken (String token) {
        String encodedToken = hashToken(token);
        return refreshTokenRepository.getByToken(encodedToken)
                .orElseThrow(() -> new SecurityException("Refresh token não encontrado."));
    }

    public String generate (User user, String ip, String deviceInfo) {
        String token = UUID.randomUUID().toString();
        String encodedToken = hashToken(token);

        RefreshToken refreshToken = RefreshToken
                .builder()
                .user(user)
                .token(encodedToken)
                .ipAddress(ip)
                .deviceInfo(deviceInfo)
                .expiresAt(getExpirationDate())
                .used(false)
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return token;
    }

    public void consume (String token) {
        String encodedToken = hashToken(token);
        refreshTokenRepository.consumeToken(encodedToken);
    }

    public void logout (String token) {
        String encodedToken = hashToken(token);
        refreshTokenRepository.revokeToken(encodedToken);
    }

    public void revokeAllByUser (User user) {
        refreshTokenRepository.revokeAllByUser(user);
    }

    @Transactional
    public void cleanInactiveTokens() {
        refreshTokenRepository.deleteExpiredAndConsumed(LocalDateTime.now());
    }

}
