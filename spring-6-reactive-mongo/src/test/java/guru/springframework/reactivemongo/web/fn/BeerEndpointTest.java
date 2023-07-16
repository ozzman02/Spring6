package guru.springframework.reactivemongo.web.fn;

import guru.springframework.reactivemongo.domain.Beer;
import guru.springframework.reactivemongo.model.BeerDTO;
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
import static guru.springframework.reactivemongo.services.BeerServiceImplTest.getTestBeer;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@AutoConfigureWebTestClient
class BeerEndpointTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    @Order(1)
    void testListBeers() {
        webTestClient.get().uri(BEER_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .expectBody().jsonPath("$.size()").value(greaterThan(1));
    }

    @Test
    @Order(2)
    void testGetById() {
        webTestClient.get().uri(BEER_PATH_ID, getSavedTestBeer().getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .expectBody(BeerDTO.class);
    }

    @Test
    @Order(3)
    void testCreateBeer() {
        webTestClient.post().uri(BEER_PATH)
                .body(Mono.just(getSavedTestBeer()), BeerDTO.class)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(LOCATION_HEADER);
    }

    @Test
    @Order(4)
    void testUpdateBeer() {
        BeerDTO updatedBeerDto = getUpdatedTestBeer();
        webTestClient.put()
                .uri(BEER_PATH_ID, updatedBeerDto.getId())
                .body(Mono.just(updatedBeerDto), BeerDTO.class)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @Order(5)
    void testPatchBeer() {
        BeerDTO beerDto = getUpdatedTestBeer();
        webTestClient.patch()
                .uri(BEER_PATH_ID, beerDto.getId())
                .body(Mono.just(beerDto), BeerDTO.class)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @Order(6)
    void testDeleteBeer() {
        webTestClient.delete()
                .uri(BEER_PATH_ID, getSavedTestBeer().getId())
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    @Order(7)
    void testCreateBeerBadData() {
        Beer testBeer = getTestBeer();
        testBeer.setBeerName("");
        webTestClient.post()
                .uri(BEER_PATH)
                .body(Mono.just(testBeer), BeerDTO.class)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(8)
    void testUpdateBeerBadRequest() {
        Beer testBeer = getTestBeer();
        testBeer.setBeerStyle("");
        webTestClient.put()
                .uri(BEER_PATH_ID, 1)
                .body(Mono.just(testBeer), BeerDTO.class)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @Order(9)
    void testGetBeerByIdNotFound() {
        webTestClient.get()
                .uri(BEER_PATH_ID, 999)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(10)
    void testUpdateBeerNotFound() {
        webTestClient.put()
                .uri(BEER_PATH_ID, 999)
                .body(Mono.just(getTestBeer()), BeerDTO.class)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(11)
    void testPatchBeerNotFound() {
        webTestClient.patch()
                .uri(BEER_PATH_ID, 999)
                .body(Mono.just(getTestBeer()), BeerDTO.class)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(12)
    void testDeleteBeerNotFound() {
        webTestClient.delete()
                .uri(BEER_PATH_ID, 999)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    @Order(13)
    void testListBeersByStyle() {
        BeerDTO testDto = getSavedTestBeer();
        testDto.setBeerStyle(TEST_BEER_STYLE);

        //create test data
        webTestClient.post().uri(BEER_PATH)
                .body(Mono.just(testDto), BeerDTO.class)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange();

        webTestClient.get().uri(UriComponentsBuilder
                        .fromPath(BEER_PATH)
                        .queryParam(BEER_STYLE_QUERY_PARAM_NAME, TEST_BEER_STYLE).build().toUri())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .expectBody().jsonPath("$.size()").value(equalTo(1));
    }

    private BeerDTO getSavedTestBeer(){
        FluxExchangeResult<BeerDTO> beerDTOFluxExchangeResult = webTestClient.post().uri(BEER_PATH)
                .body(Mono.just(getTestBeer()), BeerDTO.class)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .exchange()
                .returnResult(BeerDTO.class);

        List<String> location = beerDTOFluxExchangeResult.getResponseHeaders().get(LOCATION_HEADER);

        return webTestClient.get().uri(BEER_PATH)
                .exchange().returnResult(BeerDTO.class).getResponseBody().blockFirst();
    }

    private BeerDTO getUpdatedTestBeer() {
        BeerDTO beerDTO = getSavedTestBeer();
        beerDTO.setBeerName("New");
        return beerDTO;
    }

}