package com.splitsphere.service;

import com.splitsphere.dto.AuthResponse;
import com.splitsphere.dto.LoginRequest;
import com.splitsphere.dto.UserRegistrationRequest;
import com.splitsphere.model.User;
import com.splitsphere.repository.UserRepository;
import com.splitsphere.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditService auditService;
    
    @Transactional
    public AuthResponse registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("User ID already exists");
        }
        
        User user = new User();
        user.setAccountName(request.getAccountName());
        user.setUserId(request.getUserId());
        user.setCode(passwordEncoder.encode(request.getCode()));
        
        user = userRepository.save(user);
        
        auditService.log("CREATE", "User", user.getId(), user, "User registered");
        
        String token = jwtUtil.generateToken(user.getUserId());
        return new AuthResponse(token, user.getUserId(), user.getAccountName());
    }
    
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        
        if (!passwordEncoder.matches(request.getCode(), user.getCode())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        
        String token = jwtUtil.generateToken(user.getUserId());
        return new AuthResponse(token, user.getUserId(), user.getAccountName());
    }
    
    @Transactional(readOnly = true)
    public User getUserByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
