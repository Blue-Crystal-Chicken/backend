package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper;

import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.AddressRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.AddressResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.address.Address;

public interface AddressMapper {
    AddressResponse toResponse(Address address);
    Address toEntity(AddressRequest request);
}
