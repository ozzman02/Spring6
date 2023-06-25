package com.ossant.repositories;

import com.ossant.entities.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    CustomerRepository customerRepository;

    @Test
    void testSavedCustomer() {
        Customer customer = customerRepository.save(Customer.builder()
                .name("New Customer Name")
                .build());

        assertThat(customer.getId()).isNotNull();

    }
}