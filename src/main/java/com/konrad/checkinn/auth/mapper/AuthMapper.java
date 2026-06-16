package com.konrad.checkinn.auth.mapper;

import com.konrad.checkinn.auth.dto.LoginResponseDTO;
import com.konrad.checkinn.auth.dto.RegistrationRequestDTO;
import com.konrad.checkinn.auth.dto.RegistrationResponseDTO;
import com.konrad.checkinn.core.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "password", ignore = true),
            @Mapping(target = "roles", ignore = true),
    })
    User registrationRequestDtoToUser(RegistrationRequestDTO registrationRequestDTO);

    RegistrationResponseDTO userToRegistrationResponseDto(User user);

    @Mapping(target = "token", ignore = true)
    LoginResponseDTO userToLoginResponseDto(User user);

}
