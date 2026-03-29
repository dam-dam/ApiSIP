package com.upiicsa.ApiSIP.Service;

import com.upiicsa.ApiSIP.Dto.Cedula.AddressDto;
import com.upiicsa.ApiSIP.Exception.BusinessException;
import com.upiicsa.ApiSIP.Model.Address;
import com.upiicsa.ApiSIP.Model.Catalogs.State;
import com.upiicsa.ApiSIP.Model.Enum.ErrorCode;
import com.upiicsa.ApiSIP.Repository.AddressRepository;
import com.upiicsa.ApiSIP.Repository.Catalogs.StateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressService {

    private AddressRepository addressRepository;
    private StateRepository stateRepository;

    public AddressService(AddressRepository addressRepository, StateRepository stateRepository) {
        this.addressRepository = addressRepository;
        this.stateRepository = stateRepository;
    }

    @Transactional(readOnly = true)
    public State findStateById(Integer id){
        return stateRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Recurso: Estado"));
    }

    @Transactional
    public Address createAddress(AddressDto addressDto) {
        State state = findStateById(addressDto.stateId());

        Address newAddress = Address.builder()
                .street(addressDto.street())
                .number(addressDto.number())
                .neighborhood(addressDto.neighborhood())
                .zipCode(addressDto.zipCode())
                .state(state)
                .build();

        addressRepository.save(newAddress);
        return newAddress;
    }

    @Transactional
    public Address updateAddress(Integer addressId, AddressDto addressDto) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Recurso: Direccion"));

        if(!addressDto.street().equals(address.getStreet())){
            address.setStreet(addressDto.street());
        }
        if(!addressDto.number().equals(address.getNumber())){
            address.setNumber(addressDto.number());
        }
        if(!addressDto.zipCode().equals(address.getZipCode())){
            address.setZipCode(addressDto.zipCode());
        }
        if(!addressDto.neighborhood().equals(address.getNeighborhood())){
            address.setNeighborhood(addressDto.neighborhood());
        }
        if(!addressDto.stateId().equals(address.getState().getId())){
            State state = findStateById(addressDto.stateId());
            address.setState(state);
        }
        return addressRepository.save(address);
    }
}
