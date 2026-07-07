package com.semicolon.medibookauthservice.service;

import com.semicolon.medibookauthservice.data.models.AuthLog;
import com.semicolon.medibookauthservice.data.models.AuthUser;
import com.semicolon.medibookauthservice.data.repository.AuthLogRepository;
import com.semicolon.medibookauthservice.data.repository.AuthUserRepository;
import com.semicolon.medibookauthservice.dto.event.UserRegisteredEvent;
import com.semicolon.medibookauthservice.dto.request.LoginRequest;
import com.semicolon.medibookauthservice.dto.request.RegisterRequest;
import com.semicolon.medibookauthservice.dto.response.AuthResponse;
import com.semicolon.medibookauthservice.enums.AuthLogAction;
import com.semicolon.medibookauthservice.enums.AuthStatus;
import com.semicolon.medibookauthservice.exception.AccountDeactivatedException;
import com.semicolon.medibookauthservice.exception.InvalidCredentialsException;
import com.semicolon.medibookauthservice.exception.UserAlreadyExistException;
import com.semicolon.medibookauthservice.exception.UserNotFoundException;
import com.semicolon.medibookauthservice.kafka.producer.AuthEventProducer;
import com.semicolon.medibookauthservice.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.semicolon.medibookauthservice.utility.Mapper.map;

@Service
public class AuthService {

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private AuthLogRepository authLogRepository;

    @Autowired
    private  PasswordEncoder passwordEncoder;

    @Autowired
    private  JwtUtil jwtUtil;

    @Autowired
    private AuthEventProducer authEventProducer;

    @Autowired
    private  AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request,String ipAddress) {
        if (authUserRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistException("Email already exists");
        }

        String refreshToken = generateRefreshToken();
        AuthUser authUser = map(request,refreshToken);
        authUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        authUserRepository.save(authUser);

        saveLog(authUser, ipAddress, AuthStatus.SUCCESS,AuthLogAction.REGISTER);

        UserRegisteredEvent event = map(authUser.getId(),request);
        authEventProducer.publishUserRegisteredEvent(event);

        String token = jwtUtil.generateToken(authUser);

        return map(authUser, token);
    }


    public AuthResponse login(LoginRequest request, String ipAddress) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException exception) {
            authUserRepository.findByEmail(request.getEmail()).ifPresent(user ->
                    saveLog(user, ipAddress, AuthStatus.FAILED,AuthLogAction.LOGIN)
            );
            throw new InvalidCredentialsException("Invalid email or password");
        }

        AuthUser authUser = authUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!authUser.getIsActive()) {
            throw new AccountDeactivatedException("Account is deactivated");
        }

        saveLog(authUser, ipAddress, AuthStatus.SUCCESS,AuthLogAction.LOGIN);
        String token = jwtUtil.generateToken(authUser);

        return map(authUser, token);

    }

    public AuthResponse refresh(String refreshToken) {
        AuthUser authUser = authUserRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new UserNotFoundException("Invalid refresh token"));

        if (authUser.getRefreshTokenExpiry().isBefore(Instant.now())) {
            throw new InvalidCredentialsException("Refresh token expired, please login again");
        }

        authUser.setRefreshToken(generateRefreshToken());
        authUser.setRefreshTokenExpiry(Instant.now().plusSeconds(2592000));
        authUserRepository.save(authUser);

        String token = jwtUtil.generateToken(authUser);

        return map(authUser, token);
    }


    public void logout(String refreshToken) {
        AuthUser authUser = authUserRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new UserNotFoundException("Invalid refresh token"));

        authUser.setRefreshToken(null);
        authUser.setRefreshTokenExpiry(null);
        authUserRepository.save(authUser);
    }

    private String generateRefreshToken() {
        return java.util.UUID.randomUUID().toString();
    }

    private void saveLog(AuthUser authUser, String ipAddress, AuthStatus status, AuthLogAction action) {
        AuthLog log = new AuthLog();
        log.setAuthUser(authUser);
        log.setIpAddress(ipAddress);
        log.setStatus(status);
        log.setAction(action);
        authLogRepository.save(log);
    }

}
