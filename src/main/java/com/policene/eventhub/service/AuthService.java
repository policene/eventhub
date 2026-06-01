package com.policene.eventhub.service;

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
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService  {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyRegisteredException();
        }

        Role role = Role.CUSTOMER;

        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setRole(role);

        User saved = userRepository.save(user);

        String token = jwtService.generateToken(new UserDetailsImpl(saved));
        return new AuthResponseDTO(token);
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.email(), dto.password()));
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas");
        }

        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Credenciais invalidas"));

        String token = jwtService.generateToken(new UserDetailsImpl(user));
        return new AuthResponseDTO(token);
    }
}
