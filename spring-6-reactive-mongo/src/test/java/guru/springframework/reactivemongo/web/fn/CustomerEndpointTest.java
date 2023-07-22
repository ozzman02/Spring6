package guru.springframework.reactivemongo.web.fn;

import guru.springframework.reactivemongo.domain.Customer;
import guru.springframework.reactivemongo.model.CustomerDTO;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;

import static guru.springframework.reactivemongo.app.ApplicationConstants.*;
import static guru.springframework.reactivemongo.services.CustomerServiceImplTest.getTestCustomer;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOAuth2Login;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@AutoConfigureWebTestClient
public class CustomerEndpointTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    @Order(1)
    void testListCustomers() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .get().uri(CUSTOMER_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .expectBody().jsonPath("$.size()").value(greaterThan(1));
    }

    @Test
    @Order(2)
    void testListCustomersByName() {
        CustomerDTO testDto = getSavedTestCustomer();
        testDto.setCustomerName(TEST_CUSTOMER_NAME);

        //create test data
        webTestClient
                .mutateWith(mockOAuth2Login())
                .post().uri(CUSTOMER_PATH)
                .body(Mono.just(testDto), CustomerDTO.class)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange();

        webTestClient
                .mutateWith(mockOAuth2Login())
                .get().uri(UriComponentsBuilder
                        .fromPath(CUSTOMER_PATH)
                        .queryParam(CUSTOMER_NAME_QUERY_PARAM_NAME, TEST_CUSTOMER_NAME).build().toUri())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .expectBody().jsonPath("$.size()").value(equalTo(1));
    }

    @Test
    @Order(3)
    void getCustomerById() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .get().uri(CUSTOMER_PATH_ID, getSavedTestCustomer().getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .expectBody(CustomerDTO.class);
    }

    @Test
    @Order(4)
    void createNewCustomer() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .post().uri(CUSTOMER_PATH)
                .body(Mono.just(getTestCustomer()), CustomerDTO.class)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(LOCATION_HEADER);
    }

    @Test
    @Order(5)
    void updateCustomer() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .put()
                .uri(CUSTOMER_PATH_ID, getUpdatedTestCustomer().getId())
                .body(Mono.just(getTestCustomer()), CustomerDTO.class)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @Order(6)
    void patchCustomer() {
        CustomerDTO customerDTO = getUpdatedTestCustomer();
        webTestClient
                .mutateWith(mockOAuth2Login())
                .patch()
                .uri(CUSTOMER_PATH_ID, customerDTO.getId())
                .body(Mono.just(customerDTO), CustomerDTO.class)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @Order(7)
    void deleteCustomer() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .delete()
                .uri(CUSTOMER_PATH_ID, getSavedTestCustomer().getId())
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    @Order(8)
    void testCreateCustomerBadData() {
        Customer testCustomer = getTestCustomer();
        testCustomer.setCustomerName("");
        webTestClient
                .mutateWith(mockOAuth2Login())
                .post()
                .uri(CUSTOMER_PATH)
                .body(Mono.just(testCustomer), CustomerDTO.class)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(9)
    void testUpdateCustomerBadRequest() {
        Customer testCustomer = getTestCustomer();
        testCustomer.setCustomerName("");
        webTestClient
                .mutateWith(mockOAuth2Login())
                .put()
                .uri(CUSTOMER_PATH_ID, 1)
                .body(Mono.just(testCustomer), CustomerDTO.class)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(10)
    void testGetCustomerByIdNotFound() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .get()
                .uri(CUSTOMER_PATH_ID, 999)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(11)
    void testUpdateCustomerNotFound() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .put()
                .uri(CUSTOMER_PATH_ID, 999)
                .body(Mono.just(getTestCustomer()), CustomerDTO.class)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(12)
    void testPatchCustomerNotFound() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .patch()
                .uri(CUSTOMER_PATH_ID, 999)
                .body(Mono.just(getTestCustomer()), CustomerDTO.class)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(13)
    void testDeleteCustomerNotFound() {
        webTestClient
                .mutateWith(mockOAuth2Login())
                .delete()
                .uri(CUSTOMER_PATH_ID, 999)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    private CustomerDTO getSavedTestCustomer(){
        FluxExchangeResult<CustomerDTO> customerDTOFluxExchangeResult =
                webTestClient
                        .mutateWith(mockOAuth2Login())
                        .post().uri(CUSTOMER_PATH)
                .body(Mono.just(getTestCustomer()), CustomerDTO.class)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .returnResult(CustomerDTO.class);

        List<String> location = customerDTOFluxExchangeResult.getResponseHeaders().get(LOCATION_HEADER);

        return webTestClient
                .mutateWith(mockOAuth2Login())
                .get().uri(CUSTOMER_PATH)
                .exchange().returnResult(CustomerDTO.class).getResponseBody().blockFirst();
    }

    private CustomerDTO getUpdatedTestCustomer() {
        CustomerDTO customerDTO = getSavedTestCustomer();
        customerDTO.setCustomerName(TEST_NEW_CUSTOMER_NAME);
        return customerDTO;
    }

}
