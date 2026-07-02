package com.semicolon.medibookauthservice.utility;

import com.semicolon.medibookauthservice.data.models.AuthUser;
import com.semicolon.medibookauthservice.dto.request.RegisterRequest;
import com.semicolon.medibookauthservice.dto.response.AuthResponse;

public class Mapper {


    public static AuthUser map (RegisterRequest request){

        AuthUser authUser = new AuthUser();
        authUser.setEmail(request.getEmail());
        authUser.setRole(request.getRole());

        return authUser;
    }

    public static AuthResponse map (AuthUser authUser,String token){

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setRole(authUser.getRole());
        response.setUserId(authUser.getId());

        return response;
    }
}
