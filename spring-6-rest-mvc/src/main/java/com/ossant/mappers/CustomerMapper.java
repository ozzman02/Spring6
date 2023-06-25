package com.ossant.mappers;

import com.ossant.entities.Customer;
import com.ossant.model.CustomerDTO;
import org.mapstruct.Mapper;

@Mapper
public interface CustomerMapper {

    Customer customerDtoToCustomer(CustomerDTO customerDTO);

    CustomerDTO customerToCustomerDto(Customer customer);

}
