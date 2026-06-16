package com.konrad.checkinn.auth.dto;

import com.konrad.checkinn.core.enums.Role;
import lombok.Data;

import java.util.Set;

@Data
public class LoginResponseDTO {
    private String fullName;
    private String mobileNumber;
    private String email;
    private Set<Role> roles;
    private String token;
}
