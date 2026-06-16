package com.konrad.checkinn.core.mapper;

import com.konrad.checkinn.core.dto.HostSummaryDTO;
import com.konrad.checkinn.core.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CoreUserMapper {
    HostSummaryDTO userToHostSummaryDTO(User user);
}
