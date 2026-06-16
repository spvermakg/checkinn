package com.konrad.checkinn.auth.service;

import com.konrad.checkinn.auth.dto.LoginRequestDTO;
import com.konrad.checkinn.auth.dto.LoginResponseDTO;
import com.konrad.checkinn.auth.dto.RegistrationRequestDTO;
import com.konrad.checkinn.auth.dto.RegistrationResponseDTO;
import com.konrad.checkinn.auth.mapper.AuthMapper;
import com.konrad.checkinn.core.entity.User;
import com.konrad.checkinn.core.enums.Role;
import com.konrad.checkinn.core.exception.InvalidCredentialException;
import com.konrad.checkinn.core.exception.InvalidRequestException;
import com.konrad.checkinn.core.repository.UserRepository;
import com.konrad.checkinn.core.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegistrationRequestDTO requestDTO;
    private LoginRequestDTO loginRequestDTO;
    private User rawUser;
    private User savedUser;
    private RegistrationResponseDTO responseDTO;
    private LoginResponseDTO loginResponseDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new RegistrationRequestDTO();
        requestDTO.setEmail("test@example.com");
        requestDTO.setPassword("securePassword123");
        requestDTO.setFullName("Test User");

        loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setEmail("test@example.com");
        loginRequestDTO.setPassword("securePassword123");

        rawUser = new User();
        rawUser.setEmail("test@example.com");

        savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");
        savedUser.setRoles(Set.of(Role.GUEST));
        savedUser.setPassword("$2a$10$hashedPasswordExample");

        responseDTO = new RegistrationResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setEmail("test@example.com");

        loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setEmail("test@example.com");
        loginResponseDTO.setToken("mocked.jwt.token");
    }

    // Registration Tests

    @Test
    void registerUser_ShouldReturnResponseDTO_WhenEmailIsUnique() {
        when(userRepository.existsByEmail(requestDTO.getEmail())).thenReturn(false);
        when(authMapper.registrationRequestDtoToUser(requestDTO)).thenReturn(rawUser);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(authMapper.userToRegistrationResponseDto(savedUser)).thenReturn(responseDTO);

        RegistrationResponseDTO result = authService.registerUser(requestDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());

        verify(userRepository).existsByEmail(requestDTO.getEmail());
        verify(authMapper).registrationRequestDtoToUser(requestDTO);
        verify(userRepository).save(rawUser);
        verify(authMapper).userToRegistrationResponseDto(savedUser);
    }

    @Test
    void registerUser_ShouldReturnGenericResponse_WhenEmailExists() {
        when(userRepository.existsByEmail(requestDTO.getEmail())).thenReturn(true);

        RegistrationResponseDTO result = authService.registerUser(requestDTO);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        // Should NOT have an ID — it's a generic response, not a persisted user
        assertNull(result.getId());

        verify(userRepository).existsByEmail(requestDTO.getEmail());
        verifyNoInteractions(authMapper);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_ShouldAssignGuestRoleByDefault() {
        when(userRepository.existsByEmail(requestDTO.getEmail())).thenReturn(false);
        when(authMapper.registrationRequestDtoToUser(requestDTO)).thenReturn(rawUser);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(authMapper.userToRegistrationResponseDto(savedUser)).thenReturn(responseDTO);

        authService.registerUser(requestDTO);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertTrue(userCaptor.getValue().getRoles().contains(Role.GUEST));
    }

    @Test
    void registerUser_ShouldHashPassword() {
        when(userRepository.existsByEmail(requestDTO.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("securePassword123")).thenReturn("$2a$10$hashedResult");
        when(authMapper.registrationRequestDtoToUser(requestDTO)).thenReturn(rawUser);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(authMapper.userToRegistrationResponseDto(savedUser)).thenReturn(responseDTO);

        authService.registerUser(requestDTO);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        assertEquals("$2a$10$hashedResult", userCaptor.getValue().getPassword());
        verify(passwordEncoder).encode("securePassword123");
    }

    // Login Tests

    @Test
    void loginUser_ShouldReturnLoginResponseDTO_WhenCredentialsAreValid() {
        when(userRepository.findUserByEmail(loginRequestDTO.getEmail()))
                .thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches(loginRequestDTO.getPassword(), savedUser.getPassword()))
                .thenReturn(true);
        when(jwtService.generateToken(savedUser)).thenReturn("mocked.jwt.token");
        when(authMapper.userToLoginResponseDto(savedUser)).thenReturn(loginResponseDTO);

        LoginResponseDTO result = authService.loginUser(loginRequestDTO);

        assertNotNull(result);
        assertEquals("mocked.jwt.token", result.getToken());
        assertEquals("test@example.com", result.getEmail());

        verify(userRepository).findUserByEmail(loginRequestDTO.getEmail());
        verify(passwordEncoder).matches(loginRequestDTO.getPassword(), savedUser.getPassword());
        verify(jwtService).generateToken(savedUser);
    }

    @Test
    void loginUser_ShouldThrowInvalidCredentialException_WhenEmailDoesNotExist() {
        when(userRepository.findUserByEmail(loginRequestDTO.getEmail()))
                .thenReturn(Optional.empty());

        InvalidCredentialException exception = assertThrows(InvalidCredentialException.class,
                () -> authService.loginUser(loginRequestDTO));

        assertEquals(InvalidCredentialException.WRONG_CREDENTIALS, exception.getMessage());

        verify(userRepository).findUserByEmail(loginRequestDTO.getEmail());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void loginUser_ShouldThrowInvalidCredentialException_WhenPasswordIsWrong() {
        when(userRepository.findUserByEmail(loginRequestDTO.getEmail()))
                .thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches(loginRequestDTO.getPassword(), savedUser.getPassword()))
                .thenReturn(false);

        InvalidCredentialException exception = assertThrows(InvalidCredentialException.class,
                () -> authService.loginUser(loginRequestDTO));

        assertEquals(InvalidCredentialException.WRONG_CREDENTIALS, exception.getMessage());

        verify(userRepository).findUserByEmail(loginRequestDTO.getEmail());
        verify(passwordEncoder).matches(loginRequestDTO.getPassword(), savedUser.getPassword());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void shouldAssignHostRoleWhenHostRoleProvided() {
        // Arrange
        RegistrationRequestDTO requestDTO = new RegistrationRequestDTO();
        requestDTO.setEmail("host@example.com");
        requestDTO.setPassword("password123");
        requestDTO.setFullName("Host User");
        requestDTO.setRoles(Set.of(Role.HOST));

        User savedUser = new User();
        savedUser.setEmail("host@example.com");
        savedUser.setRoles(Set.of(Role.HOST));

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(authMapper.registrationRequestDtoToUser(any())).thenReturn(new User());
        when(authMapper.userToRegistrationResponseDto(any())).thenReturn(new RegistrationResponseDTO());

        // Act
        authService.registerUser(requestDTO);

        // Assert
        verify(userRepository).save(argThat(user ->
                user.getRoles().contains(Role.HOST)
        ));
    }

    @Test
    void shouldAssignGuestAndHostRoleWhenBothRolesProvided() {
        // Arrange
        RegistrationRequestDTO requestDTO = new RegistrationRequestDTO();
        requestDTO.setEmail("both@example.com");
        requestDTO.setPassword("password123");
        requestDTO.setFullName("Both Roles User");
        requestDTO.setRoles(Set.of(Role.GUEST, Role.HOST));

        User savedUser = new User();
        savedUser.setEmail("both@example.com");
        savedUser.setRoles(Set.of(Role.GUEST, Role.HOST));

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(authMapper.registrationRequestDtoToUser(any())).thenReturn(new User());
        when(authMapper.userToRegistrationResponseDto(any())).thenReturn(new RegistrationResponseDTO());

        // Act
        authService.registerUser(requestDTO);

        // Assert
        verify(userRepository).save(argThat(user ->
                user.getRoles().containsAll(Set.of(Role.GUEST, Role.HOST))
        ));
    }

    @Test
    void shouldThrowInvalidRequestExceptionWhenAdminRoleProvided() {
        // Arrange
        RegistrationRequestDTO requestDTO = new RegistrationRequestDTO();
        requestDTO.setEmail("admin@example.com");
        requestDTO.setPassword("password123");
        requestDTO.setFullName("Admin User");
        requestDTO.setRoles(Set.of(Role.ADMIN));

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(authMapper.registrationRequestDtoToUser(any())).thenReturn(new User());

        // Act & Assert
        assertThrows(InvalidRequestException.class, () ->
                authService.registerUser(requestDTO)
        );

        // Verify save was never called
        verify(userRepository, never()).save(any(User.class));
    }
}