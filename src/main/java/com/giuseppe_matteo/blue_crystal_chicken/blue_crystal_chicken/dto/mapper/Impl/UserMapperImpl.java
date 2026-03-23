package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.Impl;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.UserMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.UserRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.UserResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.UserEntity;
import java.time.LocalDate;

public class UserMapperImpl implements UserMapper {
    @Override
    public UserEntity toUser(UserRequest userRequest) {
        UserEntity user = new UserEntity();
        user.setName(userRequest.getName());
        user.setSurname(userRequest.getSurname());
        user.setEmail(userRequest.getEmail());
        user.setPhone(userRequest.getPhone());
        user.setGender(userRequest.getGender());
        user.setBirthday(LocalDate.parse(userRequest.getBirthday()));
        user.setPassword(userRequest.getPassword());
        user.setRole(userRequest.getRole());
        return user;
    }

    @Override
    public UserResponse toUserResponse(UserEntity user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setName(user.getName());
        userResponse.setSurname(user.getSurname());
        userResponse.setEmail(user.getEmail());
        userResponse.setPhone(user.getPhone());
        userResponse.setGender(user.getGender());
        userResponse.setBirthday(user.getBirthday().toString());
        userResponse.setRole(user.getRole());
        return userResponse;
    }
}