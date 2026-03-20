package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.UserRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.UserResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.User;

public interface UserMapper {
    User toUser(UserRequest userRequest);
    UserResponse toUserResponse(User user);
}