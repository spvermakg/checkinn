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
import com.konrad.checkinn.core.exception.UserAlreadyExistException;
import com.konrad.checkinn.core.repository.UserRepository;
import com.konrad.checkinn.core.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private static final Set<Role> SELF_ASSIGNABLE_ROLES = Set.of(Role.GUEST, Role.HOST);

    public RegistrationResponseDTO registerUser(RegistrationRequestDTO registrationRequestDTO) throws UserAlreadyExistException {
        if (isExistsByEmail(registrationRequestDTO.getEmail())) {
            RegistrationResponseDTO genericResponse = new RegistrationResponseDTO();
            genericResponse.setEmail(registrationRequestDTO.getEmail());
            genericResponse.setFullName(registrationRequestDTO.getFullName());
            return genericResponse;
        }

        String hashedPassword = createHashedPassword(registrationRequestDTO.getPassword());

        User mappedUser = authMapper.registrationRequestDtoToUser(registrationRequestDTO);
        mappedUser.setPassword(hashedPassword);

        Set<Role> requestedRoles = registrationRequestDTO.getRoles();
        if (requestedRoles != null && !requestedRoles.isEmpty()) {
            if (!SELF_ASSIGNABLE_ROLES.containsAll(requestedRoles)) {
                throw new InvalidRequestException(InvalidRequestException.INVALID_ROLE_MESSAGE);
            }
            mappedUser.setRoles(requestedRoles);
        } else {
            mappedUser.setRoles(Set.of(Role.GUEST));
        }

        User savedUser = userRepository.save(mappedUser);
        return authMapper.userToRegistrationResponseDto(savedUser);
    }

    public LoginResponseDTO loginUser(LoginRequestDTO loginRequestDTO) {

        Optional<User> user = userRepository.findUserByEmail(loginRequestDTO.getEmail());
        User foundUser = user.orElseThrow(() ->
                new InvalidCredentialException(InvalidCredentialException.WRONG_CREDENTIALS));

        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), foundUser.getPassword())) {
            throw new InvalidCredentialException(InvalidCredentialException.WRONG_CREDENTIALS);
        }

        String token = jwtService.generateToken(foundUser);
        LoginResponseDTO loginResponseDTO = authMapper.userToLoginResponseDto(foundUser);
        loginResponseDTO.setToken(token);
        return loginResponseDTO;
    }

    private String createHashedPassword(String password) {
        return passwordEncoder.encode(password);
    }

    private boolean isExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

}
