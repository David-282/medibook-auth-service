package com.semicolon.medibookauthservice.service;

import com.semicolon.medibookauthservice.data.models.AuthUser;
import com.semicolon.medibookauthservice.data.repository.AuthUserRepository;
import com.semicolon.medibookauthservice.dto.request.LoginRequest;
import com.semicolon.medibookauthservice.dto.request.RegisterRequest;
import com.semicolon.medibookauthservice.dto.response.AuthResponse;
import com.semicolon.medibookauthservice.exception.AccountDeactivatedException;
import com.semicolon.medibookauthservice.exception.InvalidCredentialsException;
import com.semicolon.medibookauthservice.exception.UserAlreadyExistException;
import com.semicolon.medibookauthservice.exception.UserNotFoundException;
import com.semicolon.medibookauthservice.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.semicolon.medibookauthservice.utility.Mapper.map;

@Service
public class AuthService {

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private  PasswordEncoder passwordEncoder;

    @Autowired
    private  JwtUtil jwtUtil;

    @Autowired
    private  AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (authUserRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistException("Email already exists");
        }

        AuthUser authUser = map(request);
        authUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        authUserRepository.save(authUser);

        String token = jwtUtil.generateToken(authUser);

        return map(authUser, token);
    }


    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException exception) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        AuthUser authUser = authUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!authUser.getIsActive()) {
            throw new AccountDeactivatedException("Account is deactivated");
        }

        String token = jwtUtil.generateToken(authUser);

        return map(authUser, token);

    }
}
