package com.splitsphere.service;

import com.splitsphere.dto.AuthResponse;
import com.splitsphere.dto.LoginRequest;
import com.splitsphere.dto.UserRegistrationRequest;
import com.splitsphere.model.User;
import com.splitsphere.repository.UserRepository;
import com.splitsphere.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private AuditService auditService;
    
    @InjectMocks
    private UserService userService;
    
    private UserRegistrationRequest registrationRequest;
    private User user;
    
    @BeforeEach
    void setUp() {
        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setAccountName("John Doe");
        registrationRequest.setUserId("john123");
        registrationRequest.setCode("1234");
        
        user = new User();
        user.setId(1L);
        user.setAccountName("John Doe");
        user.setUserId("john123");
        user.setCode("encodedCode");
    }
    
    @Test
    void testRegisterUser_Success() {
        when(userRepository.existsByUserId(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedCode");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken(anyString())).thenReturn("testToken");
        
        AuthResponse response = userService.registerUser(registrationRequest);
        
        assertNotNull(response);
        assertEquals("testToken", response.getToken());
        assertEquals("john123", response.getUserId());
        assertEquals("John Doe", response.getAccountName());
        
        verify(userRepository).save(any(User.class));
        verify(auditService).log(eq("CREATE"), eq("User"), any(), any(), anyString());
    }
    
    @Test
    void testRegisterUser_UserIdAlreadyExists() {
        when(userRepository.existsByUserId(anyString())).thenReturn(true);
        
        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(registrationRequest);
        });
        
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testLogin_Success() {
        LoginRequest loginRequest = new LoginRequest("john123", "1234");
        
        when(userRepository.findByUserId(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString())).thenReturn("testToken");
        
        AuthResponse response = userService.login(loginRequest);
        
        assertNotNull(response);
        assertEquals("testToken", response.getToken());
        assertEquals("john123", response.getUserId());
    }
    
    @Test
    void testLogin_InvalidUserId() {
        LoginRequest loginRequest = new LoginRequest("invalid", "1234");
        
        when(userRepository.findByUserId(anyString())).thenReturn(Optional.empty());
        
        assertThrows(IllegalArgumentException.class, () -> {
            userService.login(loginRequest);
        });
    }
    
    @Test
    void testLogin_InvalidCode() {
        LoginRequest loginRequest = new LoginRequest("john123", "9999");
        
        when(userRepository.findByUserId(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        
        assertThrows(IllegalArgumentException.class, () -> {
            userService.login(loginRequest);
        });
    }
}
