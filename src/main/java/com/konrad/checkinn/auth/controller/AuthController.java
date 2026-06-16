package com.konrad.checkinn.auth.controller;

import com.konrad.checkinn.auth.dto.LoginRequestDTO;
import com.konrad.checkinn.auth.dto.LoginResponseDTO;
import com.konrad.checkinn.auth.dto.RegistrationRequestDTO;
import com.konrad.checkinn.auth.dto.RegistrationResponseDTO;
import com.konrad.checkinn.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponseDTO> registerUser(@Valid @RequestBody RegistrationRequestDTO registrationRequestDTO) {
        RegistrationResponseDTO registrationResponseDTO = authService.registerUser(registrationRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationResponseDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> loginUser(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        LoginResponseDTO loginResponseDTO = authService.loginUser(loginRequestDTO);
        return  ResponseEntity.status(HttpStatus.OK).body(loginResponseDTO);
    }
}
