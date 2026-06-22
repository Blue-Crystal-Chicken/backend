package com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.Impl;

import org.springframework.stereotype.Component;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.mapper.AddressMapper;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.request.AddressRequest;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.dto.response.AddressResponse;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.address.Address;
import com.giuseppe_matteo.blue_crystal_chicken.blue_crystal_chicken.entity.address.AddressType;

@Component
public class AddressMapperImpl implements AddressMapper {

    @Override
    public AddressResponse toResponse(Address address) {
        if (address == null) {
            return null;
        }
        AddressResponse response = new AddressResponse();
        response.setId(address.getId());
        response.setType(address.getType() != null ? address.getType().name() : null);
        response.setStreet(address.getStreet());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setZipCode(address.getZipCode());
        response.setCountry(address.getCountry());
        return response;
    }

    @Override
    public Address toEntity(AddressRequest request) {
        if (request == null) {
            return null;
        }
        Address address = new Address();
        address.setType(request.getType() != null ? AddressType.parsingSicuro(request.getType()) : null);
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setZipCode(request.getZipCode());
        address.setCountry(request.getCountry());
        return address;
    }
}
