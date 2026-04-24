package com.hospital.management.mappers;

import com.hospital.management.dto.UserDTO;
import com.hospital.management.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDTO(User user);
}
