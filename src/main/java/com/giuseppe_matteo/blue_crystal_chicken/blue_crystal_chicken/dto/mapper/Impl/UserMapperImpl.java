package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.Impl;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.UserMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.UserRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.UserResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.UserEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class UserMapperImpl implements UserMapper {

    private final ModelMapper modelMapper;

    public UserMapperImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public UserEntity toUser(UserRequest userRequest) {
        if (userRequest == null) return null;
        
        UserEntity user = modelMapper.map(userRequest, UserEntity.class);
        
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
        
        // Manual conversion for birthday if needed (LocalDate to String)
        if (user.getBirthday() != null) {
            response.setBirthday(user.getBirthday().toString());
        }
        
        return response;
    }
}