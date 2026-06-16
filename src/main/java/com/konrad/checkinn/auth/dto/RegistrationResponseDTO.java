package com.konrad.checkinn.auth.dto;

import com.konrad.checkinn.core.enums.Role;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
public class RegistrationResponseDTO {
    private Long id;
    private String fullName;
    private String mobileNumber;
    private String email;
    private Set<Role> roles;
    private Instant createdAt;
    private Instant updatedAt;
}
