package com.hospital.management.services;

import com.hospital.management.dto.UserDTO;
import com.hospital.management.entities.Patient;
import com.hospital.management.entities.User;
import com.hospital.management.enums.UserRole;
import com.hospital.management.exceptions.ResourceNotFoundException;
import com.hospital.management.mappers.UserMapper;
import com.hospital.management.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        user = new Patient();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setPhone("1234567890");
        user.setRole(UserRole.PATIENT);
        user.setActive(true);

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setEmail("john.doe@example.com");
        userDTO.setPhone("1234567890");
        userDTO.setRole(UserRole.PATIENT);
        userDTO.setActive(true);
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

        UserDTO result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getFirstName());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(999L);
        });

        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void getUserByEmail_Success() {
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

        UserDTO result = userService.getUserByEmail("john.doe@example.com");

        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getEmail());
        verify(userRepository, times(1)).findByEmail("john.doe@example.com");
    }

    @Test
    void getUserByEmail_NotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserByEmail("notfound@example.com");
        });

        verify(userRepository, times(1)).findByEmail("notfound@example.com");
    }

    @Test
    void getAllUsers_Success() {
        User user2 = new Patient();
        user2.setId(2L);
        user2.setFirstName("Jane");

        UserDTO dto2 = new UserDTO();
        dto2.setId(2L);
        dto2.setFirstName("Jane");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user, user2));
        when(userMapper.toDTO(user)).thenReturn(userDTO);
        when(userMapper.toDTO(user2)).thenReturn(dto2);

        List<UserDTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUsersByRole_Success() {
        when(userRepository.findByRole(UserRole.PATIENT)).thenReturn(Arrays.asList(user));
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

        List<UserDTO> result = userService.getUsersByRole(UserRole.PATIENT);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(UserRole.PATIENT, result.get(0).getRole());
        verify(userRepository, times(1)).findByRole(UserRole.PATIENT);
    }

    @Test
    void getActiveUsers_Success() {
        when(userRepository.findByActive(true)).thenReturn(Arrays.asList(user));
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

        List<UserDTO> result = userService.getActiveUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getActive());
        verify(userRepository, times(1)).findByActive(true);
    }

    @Test
    void getUsersByRoleAndActive_Success() {
        when(userRepository.findByRoleAndActive(UserRole.PATIENT, true))
                .thenReturn(Arrays.asList(user));
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

        List<UserDTO> result = userService.getUsersByRoleAndActive(UserRole.PATIENT, true);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(UserRole.PATIENT, result.get(0).getRole());
        assertTrue(result.get(0).getActive());
        verify(userRepository, times(1)).findByRoleAndActive(UserRole.PATIENT, true);
    }

    @Test
    void getUsersLoggedInSince_Success() {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        when(userRepository.findUsersLoggedInSince(since)).thenReturn(Arrays.asList(user));
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

        List<UserDTO> result = userService.getUsersLoggedInSince(since);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findUsersLoggedInSince(since);
    }

    @Test
    void countByRole_Success() {
        when(userRepository.countByRole(UserRole.PATIENT)).thenReturn(5L);

        long result = userService.countByRole(UserRole.PATIENT);

        assertEquals(5L, result);
        verify(userRepository, times(1)).countByRole(UserRole.PATIENT);
    }

    @Test
    void countActiveUsers_Success() {
        when(userRepository.countActiveUsers()).thenReturn(10L);

        long result = userService.countActiveUsers();

        assertEquals(10L, result);
        verify(userRepository, times(1)).countActiveUsers();
    }

    @Test
    void searchUsers_Success() {
        when(userRepository.searchUsers("john")).thenReturn(Arrays.asList(user));
        when(userMapper.toDTO(any(User.class))).thenReturn(userDTO);

        List<UserDTO> result = userService.searchUsers("john");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository, times(1)).searchUsers("john");
    }
}
