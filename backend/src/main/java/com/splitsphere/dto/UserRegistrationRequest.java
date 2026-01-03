package com.splitsphere.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {
    
    @NotBlank(message = "Account name is required")
    private String accountName;
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Code is required")
    @Pattern(regexp = "^\\d{4}$", message = "Code must be exactly 4 digits")
    private String code;
}
