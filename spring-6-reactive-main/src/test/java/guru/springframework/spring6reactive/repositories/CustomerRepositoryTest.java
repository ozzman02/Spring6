package guru.springframework.spring6reactive.repositories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6reactive.configuration.DatabaseConfiguration;
import guru.springframework.spring6reactive.domain.Customer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;

@DataR2dbcTest
@Import(DatabaseConfiguration.class)
@Order(3)
public class CustomerRepositoryTest {

    @Autowired
    CustomerRepository customerRepository;

    @Test
    void saveNewCustomer() throws InterruptedException {
        customerRepository.save(getTestCustomer())
                .subscribe(customer -> {
                    System.out.println(customer.toString());
                });
        Thread.sleep(2000L);
    }

    @Test
    void testCreateJsonCustomer() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writeValueAsString(getTestCustomer()));
    }

    public static Customer getTestCustomer() {
        return Customer.builder()
                .customerName("Vladimir Bonilla")
                .build();
    }
}
