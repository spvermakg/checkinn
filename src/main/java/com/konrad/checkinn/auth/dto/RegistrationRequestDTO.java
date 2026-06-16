package com.konrad.checkinn.auth.dto;

import com.konrad.checkinn.core.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class RegistrationRequestDTO {
    @NotBlank(message = "Full name cannot be empty")
    private String fullName;

    @NotBlank(message = "Mobile Number cannot be empty")
    @Pattern(regexp = "^\\d{10}$", message = "Mobile number must be 10 digits")
    private String mobileNumber;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, message = "Password must be at least 6 character long")
    private String password;

    private Set<Role> roles;
}
