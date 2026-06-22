package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.Impl;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.AddressMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.LocationMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.LocationRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.LocationResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.location.LocationEntity;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocationMapperImpl implements LocationMapper {

    private final ModelMapper modelMapper;
    private final AddressMapper addressMapper;

    @Override
    public LocationResponse toResponse(LocationEntity location) {
        if (location == null) return null;
        LocationResponse response = modelMapper.map(location, LocationResponse.class);
        response.setAddress(addressMapper.toResponse(location.getAddress()));
        return response;
    }

    @Override
    public LocationEntity toEntity(LocationRequest request) {
        if (request == null) return null;
        LocationEntity location = modelMapper.map(request, LocationEntity.class);
        location.setAddress(addressMapper.toEntity(request.getAddress()));
        return location;
    }
}
