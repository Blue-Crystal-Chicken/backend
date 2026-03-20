package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.UserRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.UserResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.User;

public class UserMapperImpl implements UserMapper {
    @Override
    public User toUser(UserRequest userRequest) {
        User user = new User();
        user.setName(userRequest.getName());
        user.setSurname(userRequest.getSurname());
        user.setEmail(userRequest.getEmail());
        user.setPhone(userRequest.getPhone());
        user.setGender(userRequest.getGender());
        user.setBirthday(userRequest.getBirthday());
        user.setPassword(userRequest.getPassword());
        user.setRole(userRequest.getRole());
        return user;
    }

    @Override
    public UserResponse toUserResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setName(user.getName());
        userResponse.setSurname(user.getSurname());
        userResponse.setEmail(user.getEmail());
        userResponse.setPhone(user.getPhone());
        userResponse.setGender(user.getGender());
        userResponse.setBirthday(user.getBirthday());
        userResponse.setRole(user.getRole());
        return userResponse;
    }
}