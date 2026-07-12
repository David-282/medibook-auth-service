package com.semicolon.medibookauthservice.service;

import com.semicolon.medibookauthservice.data.models.AuthLog;
import com.semicolon.medibookauthservice.data.models.AuthUser;
import com.semicolon.medibookauthservice.data.repository.AuthLogRepository;
import com.semicolon.medibookauthservice.data.repository.AuthUserRepository;
import com.semicolon.medibookauthservice.dto.event.UserRegisteredApplicationEvent;
import com.semicolon.medibookauthservice.dto.event.UserRegisteredEvent;
import com.semicolon.medibookauthservice.dto.request.LoginRequest;
import com.semicolon.medibookauthservice.dto.request.RegisterRequest;
import com.semicolon.medibookauthservice.dto.response.AuthResponse;
import com.semicolon.medibookauthservice.enums.AccountStatus;
import com.semicolon.medibookauthservice.enums.AuthLogAction;
import com.semicolon.medibookauthservice.enums.AuthStatus;
import com.semicolon.medibookauthservice.exception.*;
import com.semicolon.medibookauthservice.kafka.producer.AuthEventProducer;
import com.semicolon.medibookauthservice.security.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;

import static com.semicolon.medibookauthservice.utility.Mapper.map;
@RequiredArgsConstructor
@Service
public class AuthService {

    private final ApplicationEventPublisher applicationEventPublisher;

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

    @Transactional
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
//        authEventProducer.publishUserRegisteredEvent(event);

        applicationEventPublisher.publishEvent(new UserRegisteredApplicationEvent(event));
        String token = jwtUtil.generateToken(authUser);

        return map(authUser, token);
    }


    @Transactional
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

        if (authUser.getAccountStatus() == AccountStatus.DEACTIVATED) {
            throw new AccountDeactivatedException("Account is deactivated");

        }

        if (authUser.getAccountStatus() == AccountStatus.PENDING_PROFILE) {
            throw new AccountNotActivatedException("Please wait while your profile is being finalized.");
        }

        if (authUser.getAccountStatus() == AccountStatus.REGISTRATION_FAILED) {
            throw new RegistrationIncompleteException("Profile creation failed. Please restart the setup process.");
        }

        saveLog(authUser, ipAddress, AuthStatus.SUCCESS,AuthLogAction.LOGIN);
        String token = jwtUtil.generateToken(authUser);

        return map(authUser, token);

    }

    @Transactional
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


    @Transactional
    public void logout(String refreshToken) {
        AuthUser authUser = authUserRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new UserNotFoundException("Invalid refresh token"));

        authUser.setRefreshToken(null);
        authUser.setRefreshTokenExpiry(null);
        authUserRepository.save(authUser);
    }

    @Transactional
    public AuthResponse validateToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new InvalidCredentialsException("Invalid or expired token");
        }

        String email = jwtUtil.extractEmail(token);
        AuthUser authUser = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (authUser.getAccountStatus() == AccountStatus.DEACTIVATED) {
            throw new AccountDeactivatedException("Account is deactivated");
        }

        return map(authUser, token);
//        AuthResponse response = new AuthResponse();
//        response.setToken(token);
//        response.setRole(authUser.getRole());
//        response.setUserId(authUser.getId());
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
