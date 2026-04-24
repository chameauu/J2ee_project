package com.hospital.management.services;

import com.hospital.management.dto.UserDTO;
import com.hospital.management.enums.UserRole;

import java.time.LocalDateTime;
import java.util.List;

public interface IUserService {
    UserDTO getUserById(Long id);
    UserDTO getUserByEmail(String email);
    List<UserDTO> getAllUsers();
    List<UserDTO> getUsersByRole(UserRole role);
    List<UserDTO> getActiveUsers();
    List<UserDTO> getUsersByRoleAndActive(UserRole role, Boolean active);
    List<UserDTO> getUsersLoggedInSince(LocalDateTime since);
    long countByRole(UserRole role);
    long countActiveUsers();
    List<UserDTO> searchUsers(String keyword);
}
