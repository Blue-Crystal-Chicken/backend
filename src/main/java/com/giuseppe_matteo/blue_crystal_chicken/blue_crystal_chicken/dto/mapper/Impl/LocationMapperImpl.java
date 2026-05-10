package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.Impl;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.LocationMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.LocationRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.LocationResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.LocationEntity;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocationMapperImpl implements LocationMapper {

    private final ModelMapper modelMapper;

    @Override
    public LocationResponse toResponse(LocationEntity location) {
        if (location == null) return null;
        return modelMapper.map(location, LocationResponse.class);
    }

    @Override
    public LocationEntity toEntity(LocationRequest request) {
        if (request == null) return null;
        return modelMapper.map(request, LocationEntity.class);
    }
}
