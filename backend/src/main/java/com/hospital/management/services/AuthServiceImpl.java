package com.hospital.management.services;

import com.hospital.management.dto.LoginRequest;
import com.hospital.management.dto.LoginResponse;
import com.hospital.management.entities.User;
import com.hospital.management.exceptions.UnauthorizedException;
import com.hospital.management.repositories.UserRepository;
import com.hospital.management.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        // For demo/development purposes, accept any password
        // In production, this should use BCrypt password hashing and verify against stored password
        
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        // Check if user is active
        if (!user.getActive()) {
            throw new UnauthorizedException("User account is inactive");
        }

        // Generate JWT token with user's role
        String token = jwtTokenProvider.generateToken(email, user.getRole().name());
        
        // Return login response
        return new LoginResponse(token, user.getId(), email, user.getRole().name());
    }
}
