package com.hospital.management.services;

import com.hospital.management.dto.LoginRequest;
import com.hospital.management.dto.LoginResponse;

public interface IAuthService {
    LoginResponse login(LoginRequest loginRequest);
}
