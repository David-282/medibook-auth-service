package com.semicolon.medibookauthservice.security;

import com.semicolon.medibookauthservice.data.models.AuthUser;
import com.semicolon.medibookauthservice.data.repository.AuthUserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AuthUserRepository authUserRepository;

    @Override
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        AuthUser authUser =
                authUserRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new User(
                authUser.getEmail(),
                authUser.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + authUser.getRole().name()))
        );
    }
}
