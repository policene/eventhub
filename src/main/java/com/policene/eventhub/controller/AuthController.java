package com.policene.eventhub.controller;

import com.policene.eventhub.dto.auth.AuthResponseDTO;
import com.policene.eventhub.dto.auth.LoginRequestDTO;
import com.policene.eventhub.dto.auth.RefreshTokenRequestDTO;
import com.policene.eventhub.dto.auth.RegisterRequestDTO;
import com.policene.eventhub.service.AuthService;
import com.policene.eventhub.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }

    private String extractIp (HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private String extractDeviceInfo (HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto, HttpServletRequest request) {
        String ip = extractIp(request);
        String deviceInfo = extractDeviceInfo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(dto, ip, deviceInfo));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto, HttpServletRequest request) {
        String ip = extractIp(request);
        String deviceInfo = extractDeviceInfo(request);
        return ResponseEntity.ok(authService.login(dto, ip, deviceInfo));
    }

    @PatchMapping("/logout")
    public ResponseEntity<Void> logout (@Valid @RequestBody RefreshTokenRequestDTO dto) {
        refreshTokenService.logout(dto.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh (@Valid @RequestBody RefreshTokenRequestDTO dto, HttpServletRequest request) {
        String ip = extractIp(request);
        String deviceInfo = extractDeviceInfo(request);
        return ResponseEntity.ok(authService.refresh(dto, ip, deviceInfo));
    }

}
