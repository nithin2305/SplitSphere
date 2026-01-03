package com.splitsphere.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CorsConfigurationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testCorsPreflightRequest_ExpensesEndpoint() throws Exception {
        mockMvc.perform(options("/api/expenses")
                .header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type,Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }
    
    @Test
    void testCorsPreflightRequest_GroupsEndpoint() throws Exception {
        mockMvc.perform(options("/api/groups")
                .header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type,Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }
    
    @Test
    void testCorsPreflightRequest_AuthEndpoint() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                .header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }
    
    @Test
    void testCorsActualRequest_AuthRegister() throws Exception {
        String registerJson = "{\"userId\":\"testuser\",\"code\":\"testcode\",\"accountName\":\"Test User\"}";
        
        // This will fail authentication but should still have CORS headers
        mockMvc.perform(post("/api/auth/register")
                .header("Origin", "http://localhost:4200")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }
    
    @Test
    void testCorsWithWildcardOrigin() throws Exception {
        mockMvc.perform(options("/api/expenses")
                .header("Origin", "https://example.com")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type,Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }
}
