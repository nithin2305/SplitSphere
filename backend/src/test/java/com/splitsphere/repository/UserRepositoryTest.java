package com.splitsphere.repository;

import com.splitsphere.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void testSaveAndFindUser() {
        User user = new User();
        user.setAccountName("Test User");
        user.setUserId("testuser");
        user.setCode("1234");
        
        User savedUser = userRepository.save(user);
        
        assertNotNull(savedUser.getId());
        assertEquals("testuser", savedUser.getUserId());
    }
    
    @Test
    void testFindByUserId() {
        User user = new User();
        user.setAccountName("Test User");
        user.setUserId("testuser");
        user.setCode("1234");
        
        userRepository.save(user);
        
        Optional<User> found = userRepository.findByUserId("testuser");
        
        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().getAccountName());
    }
    
    @Test
    void testExistsByUserId() {
        User user = new User();
        user.setAccountName("Test User");
        user.setUserId("testuser");
        user.setCode("1234");
        
        userRepository.save(user);
        
        assertTrue(userRepository.existsByUserId("testuser"));
        assertFalse(userRepository.existsByUserId("nonexistent"));
    }
}
