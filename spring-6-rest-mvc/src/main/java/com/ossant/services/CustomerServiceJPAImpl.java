package com.ossant.services;

import com.ossant.entities.Customer;
import com.ossant.mappers.CustomerMapper;
import com.ossant.model.CustomerDTO;
import com.ossant.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Primary
@RequiredArgsConstructor
public class CustomerServiceJPAImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    private final CustomerMapper customerMapper;

    @Override
    public Optional<CustomerDTO> getCustomerById(UUID uuid) {
        return Optional.ofNullable(customerMapper.customerToCustomerDto(customerRepository.findById(uuid).orElse(null)));
    }

    @Override
    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(customerMapper::customerToCustomerDto)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerDTO saveNewCustomer(CustomerDTO customerDTO) {
        return customerMapper.customerToCustomerDto(customerRepository.save(customerMapper.customerDtoToCustomer(customerDTO))) ;
    }

    @Override
    public Optional<CustomerDTO> updateCustomerById(UUID customerId, CustomerDTO customerDTO) {
        AtomicReference<Optional<CustomerDTO>> atomicReference = new AtomicReference<>();
        customerRepository.findById(customerId).ifPresentOrElse(foundCustomer -> {
            foundCustomer.setName(customerDTO.getName());
            foundCustomer.setUpdateDate(LocalDateTime.now());
            atomicReference.set(Optional.of(customerMapper.customerToCustomerDto(foundCustomer)));
            customerRepository.save(foundCustomer);
        }, ()-> {
            atomicReference.set(Optional.empty());
        });
        return atomicReference.get();
    }

    @Override
    public Boolean deleteCustomerById(UUID customerId) {
        if (customerRepository.existsById(customerId)) {
            customerRepository.deleteById(customerId);
            return true;
        }
        return false;
    }

    @Override
    public Boolean patchCustomerById(UUID customerId, CustomerDTO customerDTO) {
        if (customerRepository.existsById(customerId)) {
            Customer existing = customerRepository.findById(customerId).get();
            if (StringUtils.hasText(customerDTO.getName())) {
                existing.setName(customerDTO.getName());
            }
            customerRepository.save(existing);
            return true;
        }
        return false;
    }
}
