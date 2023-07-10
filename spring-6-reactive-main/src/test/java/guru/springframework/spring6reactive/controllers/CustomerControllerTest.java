package guru.springframework.spring6reactive.controllers;

import guru.springframework.spring6reactive.domain.Customer;
import guru.springframework.spring6reactive.model.CustomerDTO;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static guru.springframework.spring6reactive.app.ApplicationConstants.CUSTOMER_PATH;
import static guru.springframework.spring6reactive.app.ApplicationConstants.CUSTOMER_PATH_ID;
import static guru.springframework.spring6reactive.repositories.CustomerRepositoryTest.getTestCustomer;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(1)
class CustomerControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    @Order(1)
    void listCustomers() {
        webTestClient.get().uri(CUSTOMER_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .expectBody().jsonPath("$.size()").isEqualTo(3);
    }

    @Test
    @Order(2)
    void getCustomerById() {
        webTestClient.get().uri(CUSTOMER_PATH_ID, 1)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .expectBody(CustomerDTO.class);
    }

    @Test
    @Order(3)
    void createNewCustomer() {
        webTestClient.post().uri(CUSTOMER_PATH)
                .body(Mono.just(getTestCustomer()), CustomerDTO.class)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location("http://localhost:8080/api/v2/customer/4");
    }

    @Test
    @Order(4)
    void updateCustomer() {
        webTestClient.put()
                .uri(CUSTOMER_PATH_ID, 1)
                .body(Mono.just(getTestCustomer()), CustomerDTO.class)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @Order(5)
    void patchCustomer() {
        webTestClient.patch()
                .uri(CUSTOMER_PATH_ID, 1)
                .body(Mono.just(getTestCustomer()), CustomerDTO.class)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @Order(6)
    void deleteCustomer() {
        webTestClient.delete()
                .uri(CUSTOMER_PATH_ID, 1)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    @Order(7)
    void testCreateCustomerBadData() {
        Customer testCustomer = getTestCustomer();
        testCustomer.setCustomerName("");
        webTestClient.post()
                .uri(CUSTOMER_PATH)
                .body(Mono.just(testCustomer), CustomerDTO.class)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(8)
    void testUpdateCustomerBadRequest() {
        Customer testCustomer = getTestCustomer();
        testCustomer.setCustomerName("");
        webTestClient.put()
                .uri(CUSTOMER_PATH_ID, 1)
                .body(Mono.just(testCustomer), CustomerDTO.class)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(9)
    void testGetCustomerByIdNotFound() {
        webTestClient.get()
                .uri(CUSTOMER_PATH_ID, 999)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(10)
    void testUpdateBeerNotFound() {
        webTestClient.put()
                .uri(CUSTOMER_PATH_ID, 999)
                .body(Mono.just(getTestCustomer()), CustomerDTO.class)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(11)
    void testPatchBeerNotFound() {
        webTestClient.patch()
                .uri(CUSTOMER_PATH_ID, 999)
                .body(Mono.just(getTestCustomer()), CustomerDTO.class)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(12)
    void testDeleteBeerNotFound() {
        webTestClient.delete()
                .uri(CUSTOMER_PATH_ID, 999)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus()
                .isNotFound();
    }
}