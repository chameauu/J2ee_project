package com.hospital.management.exceptions;

import com.hospital.management.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GlobalExceptionHandler.
 */
class GlobalExceptionHandlerTest {
    
    private GlobalExceptionHandler exceptionHandler;
    
    @Mock
    private HttpServletRequest request;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exceptionHandler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/test");
    }
    
    @Test
    void shouldHandleResourceNotFoundException() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Patient", "id", 1L);
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFound(exception, request);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("Patient not found"));
        assertEquals("/api/test", response.getBody().getPath());
    }
    
    @Test
    void shouldHandleDuplicateResourceException() {
        // Given
        DuplicateResourceException exception = new DuplicateResourceException("Patient", "email", "test@example.com");
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDuplicateResource(exception, request);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("already exists"));
    }
    
    @Test
    void shouldHandleBadRequestException() {
        // Given
        BadRequestException exception = new BadRequestException("Invalid input data");
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequest(exception, request);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Invalid input data", response.getBody().getMessage());
    }
    
    @Test
    void shouldHandleUnauthorizedException() {
        // Given
        UnauthorizedException exception = new UnauthorizedException("Access denied");
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUnauthorized(exception, request);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Access denied", response.getBody().getMessage());
    }
    
    @Test
    void shouldHandleGenericException() {
        // Given
        Exception exception = new RuntimeException("Unexpected error");
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("unexpected error"));
    }
}
