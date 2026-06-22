package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.Impl;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.AddressMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.UserMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.UserRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.UserResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class UserMapperImpl implements UserMapper {

    private final ModelMapper modelMapper;
    private final AddressMapper addressMapper;

    @Override
    public UserEntity toUser(UserRequest userRequest) {
        if (userRequest == null) return null;
        
        UserEntity user = modelMapper.map(userRequest, UserEntity.class);
        user.setAddress(addressMapper.toEntity(userRequest.getAddress()));
        
        // Manual conversion for birthday if needed (String to LocalDate)
        if (userRequest.getBirthday() != null) {
            user.setBirthday(LocalDate.parse(userRequest.getBirthday()));
        }
        
        return user;
    }

    @Override
    public UserResponse toUserResponse(UserEntity user) {
        if (user == null) return null;
        
        UserResponse response = modelMapper.map(user, UserResponse.class);
        response.setAddress(addressMapper.toResponse(user.getAddress()));
        
        // Manual conversion for birthday if needed (LocalDate to String)
        if (user.getBirthday() != null) {
            response.setBirthday(user.getBirthday().toString());
        }
        
        return response;
    }

    @Override
    public UserEntity toUser(Long userId) {
        if (userId == null) return null;
        UserEntity user = new UserEntity();
        user.setId(userId);
        return user;
    }
}