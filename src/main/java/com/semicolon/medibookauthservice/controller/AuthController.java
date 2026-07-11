package com.semicolon.medibookauthservice.controller;


import com.semicolon.medibookauthservice.dto.request.LoginRequest;
import com.semicolon.medibookauthservice.dto.request.RegisterRequest;
import com.semicolon.medibookauthservice.dto.response.ApiResponse;
import com.semicolon.medibookauthservice.exception.AccountDeactivatedException;
import com.semicolon.medibookauthservice.exception.InvalidCredentialsException;
import com.semicolon.medibookauthservice.exception.UserAlreadyExistException;
import com.semicolon.medibookauthservice.exception.UserNotFoundException;
import com.semicolon.medibookauthservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private AuthService authService;

    @Transactional
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request,
                                                HttpServletRequest httpRequest
                                                ) {
        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Registration successful", authService.register(request, httpRequest.getRemoteAddr())));
        } catch (UserAlreadyExistException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request,
                                             HttpServletRequest httpRequest
                                             ) {
        try {
            return ResponseEntity
                    .ok(ApiResponse.success("Login successful", authService.login(request, httpRequest.getRemoteAddr())));
        } catch (UserNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (AccountDeactivatedException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (InvalidCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refresh(@RequestBody String refreshToken) {
        try {
            return ResponseEntity
                    .ok(ApiResponse.success("Token refreshed", authService.refresh(refreshToken)));
        } catch (UserNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (InvalidCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse> validateToken(@RequestParam String token) {
        try {
            return ResponseEntity
                    .ok(ApiResponse.success("Token is valid", authService.validateToken(token)));
        } catch (InvalidCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (UserNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (AccountDeactivatedException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestBody String refreshToken) {
        try {
            authService.logout(refreshToken);
            return ResponseEntity
                    .ok(ApiResponse.success("Logout successful", null));
        } catch (UserNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
