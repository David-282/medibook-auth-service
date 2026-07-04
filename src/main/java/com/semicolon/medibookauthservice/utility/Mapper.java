package com.semicolon.medibookauthservice.utility;

import com.semicolon.medibookauthservice.data.models.AuthUser;
import com.semicolon.medibookauthservice.dto.request.RegisterRequest;
import com.semicolon.medibookauthservice.dto.response.AuthResponse;

import java.time.Instant;

public class Mapper {


    public static AuthUser map (RegisterRequest request,String refreshToken){

        AuthUser authUser = new AuthUser();
        authUser.setEmail(request.getEmail());
        authUser.setRole(request.getRole());
        authUser.setRefreshToken(refreshToken);
        authUser.setRefreshTokenExpiry(Instant.now().plusSeconds(2592000));


        return authUser;
    }

    public static AuthResponse map (AuthUser authUser,String token){

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setRole(authUser.getRole());
        response.setUserId(authUser.getId());
        response.setRefreshToken(authUser.getRefreshToken());

        return response;
    }
}
