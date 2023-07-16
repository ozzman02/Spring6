package guru.springframework.reactivemongo.services;

import guru.springframework.reactivemongo.domain.Customer;
import guru.springframework.reactivemongo.mappers.CustomerMapper;
import guru.springframework.reactivemongo.mappers.CustomerMapperImpl;
import guru.springframework.reactivemongo.model.CustomerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static guru.springframework.reactivemongo.app.ApplicationConstants.TEST_NEW_CUSTOMER_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
public class CustomerServiceImplTest {

    @Autowired
    CustomerService customerService;

    @Autowired
    CustomerMapper customerMapper;

    CustomerDTO customerDTO;

    @BeforeEach
    void setUp() {
        customerDTO = customerMapper.customerToCustomerDto(getTestCustomer());
    }

    @Test
    void testSaveCustomerUseSubscriber() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<CustomerDTO> atomicDto = new AtomicReference<>();
        Mono<CustomerDTO> savedCustomerMono = customerService.saveCustomer(Mono.just(customerDTO));
        savedCustomerMono.subscribe(savedCustomerDto -> {
            System.out.println(savedCustomerDto.getId());
            atomicBoolean.set(true);
            atomicDto.set(savedCustomerDto);
        });
        await().untilTrue(atomicBoolean);
        CustomerDTO persistedCustomerDto = atomicDto.get();
        assertThat(persistedCustomerDto).isNotNull();
        assertThat(persistedCustomerDto.getId()).isNotNull();
    }

    @Test
    void testSaveCustomerUseBlock() {
        CustomerDTO savedCustomerDto = customerService.saveCustomer(Mono.just(getTestCustomerDto())).block();
        assertThat(savedCustomerDto).isNotNull();
        assertThat(savedCustomerDto.getId()).isNotNull();
    }

    @Test
    void testUpdateCustomerBlocking() {
        CustomerDTO savedCustomerDto = getSavedCustomerDto();
        savedCustomerDto.setCustomerName(TEST_NEW_CUSTOMER_NAME);

        CustomerDTO updatedCustomerDto = customerService.saveCustomer(Mono.just(savedCustomerDto)).block();

        //verify exists in db
        assert updatedCustomerDto != null;
        CustomerDTO fetchedCustomerDto = customerService.getCustomerById(updatedCustomerDto.getId()).block();
        assert fetchedCustomerDto != null;
        assertThat(fetchedCustomerDto.getCustomerName()).isEqualTo(TEST_NEW_CUSTOMER_NAME);
    }

    @Test
    void testUpdateCustomerStreaming() {
        AtomicReference<CustomerDTO> atomicDto = new AtomicReference<>();
        customerService.saveCustomer(Mono.just(getTestCustomerDto()))
                .map(savedCustomerDto -> {
                    savedCustomerDto.setCustomerName(TEST_NEW_CUSTOMER_NAME);
                    return savedCustomerDto;
                })
                .flatMap(customerService::saveCustomer) // save updated customer
                .flatMap(savedUpdatedCustomerDto ->
                        customerService.getCustomerById(savedUpdatedCustomerDto.getId())) // get from db
                .subscribe(atomicDto::set);

        await().until(() -> atomicDto.get() != null);
        assertThat(atomicDto.get().getCustomerName()).isEqualTo(TEST_NEW_CUSTOMER_NAME);
    }

    @Test
    void testPatchCustomerStreaming() {
        AtomicReference<CustomerDTO> atomicDto = new AtomicReference<>();
        CustomerDTO savedCustomerDto = getSavedCustomerDto();
        savedCustomerDto.setCustomerName(TEST_NEW_CUSTOMER_NAME);
        customerService.patchCustomer(savedCustomerDto.getId(), savedCustomerDto)
                .flatMap(patchedCustomerDto ->
                        customerService.getCustomerById(patchedCustomerDto.getId()))
                .subscribe(atomicDto::set);
        await().until(() -> atomicDto.get() != null);
        assertThat(atomicDto.get().getCustomerName()).isEqualTo(TEST_NEW_CUSTOMER_NAME);
    }

    @Test
    void testDeleteCustomer() {
        CustomerDTO customerToDelete = getSavedCustomerDto();
        customerService.deleteCustomer(customerToDelete.getId()).block();
        Mono<CustomerDTO> expectedEmptyCustomerDtoMono =
                customerService.getCustomerById(customerToDelete.getId());
        CustomerDTO emptyCustomerDto = expectedEmptyCustomerDtoMono.block();
        assertThat(emptyCustomerDto).isNull();
    }

    @Test
    void testFindByCustomerName() {
        CustomerDTO customerDto = getSavedCustomerDto();
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<CustomerDTO> atomicReference = new AtomicReference<>();
        customerService.findByCustomerName(customerDto.getCustomerName())
                .subscribe(dto -> {
                    System.out.println(dto.toString());
                    atomicBoolean.set(true);
                    atomicReference.set(dto);
                });
        await().untilTrue(atomicBoolean);
        assertThat(atomicReference.get().getCustomerName()).isNotNull();
    }

    public CustomerDTO getSavedCustomerDto() {
        return customerService.saveCustomer(Mono.just(getTestCustomerDto())).block();
    }

    public static CustomerDTO getTestCustomerDto() {
        return new CustomerMapperImpl().customerToCustomerDto(getTestCustomer());
    }

    public static Customer getTestCustomer() {
        return Customer.builder()
                .customerName("Customer Name Test")
                .build();
    }
}
