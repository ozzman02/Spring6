package com.ossant.services;

import com.ossant.model.CustomerDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerService {

    List<CustomerDTO> getAllCustomers();

    Optional<CustomerDTO> getCustomerById(UUID uuid);

    CustomerDTO saveNewCustomer(CustomerDTO customerDTO);

    Optional<CustomerDTO> updateCustomerById(UUID customerId, CustomerDTO customerDTO);

    Boolean deleteCustomerById(UUID customerId);

    Boolean patchCustomerById(UUID customerId, CustomerDTO customerDTO);

}
