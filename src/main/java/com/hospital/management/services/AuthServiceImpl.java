package com.hospital.management.services;

import com.hospital.management.dto.LoginRequest;
import com.hospital.management.dto.LoginResponse;
import com.hospital.management.exceptions.UnauthorizedException;
import com.hospital.management.repositories.DoctorRepository;
import com.hospital.management.repositories.PatientRepository;
import com.hospital.management.repositories.PharmacistRepository;
import com.hospital.management.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements IAuthService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PharmacistRepository pharmacistRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        // For demo purposes, we'll use a simple password check
        // In production, this should use BCrypt password hashing
        
        // Check if user exists in any repository
        if (patientRepository.existsByEmail(email)) {
            // Simple demo password: "patient123"
            if ("patient123".equals(password)) {
                String token = jwtTokenProvider.generateToken(email, "PATIENT");
                return new LoginResponse(token, email, "PATIENT");
            }
        } else if (doctorRepository.existsByEmail(email)) {
            // Simple demo password: "doctor123"
            if ("doctor123".equals(password)) {
                String token = jwtTokenProvider.generateToken(email, "DOCTOR");
                return new LoginResponse(token, email, "DOCTOR");
            }
        } else if (pharmacistRepository.existsByEmail(email)) {
            // Simple demo password: "pharmacist123"
            if ("pharmacist123".equals(password)) {
                String token = jwtTokenProvider.generateToken(email, "PHARMACIST");
                return new LoginResponse(token, email, "PHARMACIST");
            }
        }

        throw new UnauthorizedException("Invalid email or password");
    }
}
