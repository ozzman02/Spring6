package guru.springframework.spring6resttemplate.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6resttemplate.config.OAuthClientInterceptor;
import guru.springframework.spring6resttemplate.config.RestTemplateBuilderConfig;
import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerDTOPageImpl;
import guru.springframework.spring6resttemplate.model.BeerStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;


@RestClientTest
@Import(RestTemplateBuilderConfig.class)
public class BeerClientMockTest {

    static final String URL = "http://localhost:8080";

    static final String BEARER_TEST = "Bearer test";

    @Autowired
    RestTemplateBuilder restTemplateBuilderConfigured;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ClientRegistrationRepository clientRegistrationRepository;

    @Mock
    RestTemplateBuilder mockRestTemplateBuilder =
            new RestTemplateBuilder(new MockServerRestTemplateCustomizer());

    @MockBean
    OAuth2AuthorizedClientManager manager;

    BeerClient beerClient;

    MockRestServiceServer server;

    BeerDTO dto;

    String dtoJson;

    @TestConfiguration
    public static class TestConfig {

        @Bean
        ClientRegistrationRepository clientRegistrationRepository() {
            return new InMemoryClientRegistrationRepository(ClientRegistration
                    .withRegistrationId("springauth")
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .clientId("test")
                    .tokenUri("test")
                    .build());
        }

        @Bean
        OAuth2AuthorizedClientService auth2AuthorizedClientService(ClientRegistrationRepository clientRegistrationRepository){
            return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
        }

        @Bean
        OAuthClientInterceptor oAuthClientInterceptor(OAuth2AuthorizedClientManager manager,
                                                      ClientRegistrationRepository clientRegistrationRepository){
            return new OAuthClientInterceptor(manager, clientRegistrationRepository);
        }
    }

    @BeforeEach
    void setUp() throws JsonProcessingException {
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("springauth");

        OAuth2AccessToken token = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                "test", Instant.MIN, Instant.MAX);

        when(manager.authorize(any())).thenReturn(new OAuth2AuthorizedClient(clientRegistration,
                "test", token));

        RestTemplate restTemplate = restTemplateBuilderConfigured.build();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        when(mockRestTemplateBuilder.build()).thenReturn(restTemplate);
        beerClient = new BeerClientImpl(mockRestTemplateBuilder);
        dto = getBeerDto();
        dtoJson = objectMapper.writeValueAsString(dto);
    }

    /*
        This unit test is for our Beer Client to list our beers.
        We are using a rest template client underneath the covers and by default it is going to be looking
        to connect to an actual server. We need an endpoint to test with.

        We want to return a json payload since we are mimic a web application.

        We can configure a mock server and code our interaction but, we will have the problem that we will
        not be able to use the rest template because there is a binding issue.

        Problem:

            We are using that Rest Template builder inside the class and creating a new Rest Template and
            that one isn't getting bound to the server.

        Workaround:

            Get the RestTemplate bound to the MockServer and use Mockito to create a mock that will
            return back the template from the builder method.

            1. From the context we want our restTemplateBuilderConfigured.
            2. We no longer need the beerClient nor the MockRestServiceServer since we are going to configure them
                based on our needs. That's what we do in the setup method.

        Spring Security Basic implementation:

            After implementing Spring Security Basic we need to configure the RestTemplate with Basic Auth.
            The server mock will allow all the test to pass but, we want to check if the authorization header is there.
            In our tests we need to add andExpect(header("Authorization", "Basic dXNlcjpwYXNzd29yZA=="))
    */
    @Test
    void testListBeers() throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(getPage());

        server.expect(method(HttpMethod.GET))
                //.andExpect(header("Authorization", "Basic dXNlcjpwYXNzd29yZA=="))
                .andExpect(header("Authorization", BEARER_TEST))
                .andExpect(requestTo(URL + BeerClientImpl.GET_BEER_PATH))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        Page<BeerDTO> beerDtoPage = beerClient.listBeers();
        assertThat(beerDtoPage.getContent().size()).isGreaterThan(0);
    }

    @Test
    void testListBeersWithQueryParam() throws JsonProcessingException {
        String response = objectMapper.writeValueAsString(getPage());

        URI uri = UriComponentsBuilder.fromHttpUrl(URL + BeerClientImpl.GET_BEER_PATH)
                .queryParam("beerName", "Magic Apple")
                .build().toUri();

        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(uri))
                //.andExpect(header("Authorization", "Basic dXNlcjpwYXNzd29yZA=="))
                .andExpect(header("Authorization", BEARER_TEST))
                .andExpect(queryParam("beerName", "Magic%20Apple"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        Page<BeerDTO> responsePage = beerClient
                .listBeers("Magic Apple", null, null, null, null);

        assertThat(responsePage.getContent().size()).isEqualTo(1);
    }

    @Test
    void testGetBeerById() {
        mockGetByIdOperation();
        BeerDTO beerDtoResponseObj = beerClient.getBeerById(dto.getId());
        assertThat(beerDtoResponseObj.getId()).isEqualTo(dto.getId());
    }

    @Test
    void testCreateBeer() {
        URI uri = UriComponentsBuilder.fromPath(BeerClientImpl.GET_BEER_BY_ID_PATH)
                .build(dto.getId());

        server.expect(method(HttpMethod.POST))
                .andExpect(requestTo(URL + BeerClientImpl.GET_BEER_PATH))
                //.andExpect(header("Authorization", "Basic dXNlcjpwYXNzd29yZA=="))
                .andExpect(header("Authorization", BEARER_TEST))
                .andRespond(withAccepted().location(uri));

        mockGetByIdOperation();

        BeerDTO responseDto = beerClient.createBeer(dto);
        assertThat(responseDto.getId()).isEqualTo(dto.getId());
    }

    @Test
    void testUpdateBeer() {
        server.expect(method(HttpMethod.PUT))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH, dto.getId()))
                .andExpect(header("Authorization", BEARER_TEST))
                //.andExpect(header("Authorization", "Basic dXNlcjpwYXNzd29yZA=="))
                .andRespond(withNoContent());

        mockGetByIdOperation();

        BeerDTO responseDto = beerClient.updateBeer(dto);
        assertThat(responseDto.getId()).isEqualTo(dto.getId());
    }

    @Test
    void testDeleteBeer() {
        server.expect(method(HttpMethod.DELETE))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH,
                        dto.getId()))
                //.andExpect(header("Authorization", "Basic dXNlcjpwYXNzd29yZA=="))
                .andExpect(header("Authorization", BEARER_TEST))
                .andRespond(withNoContent());

        beerClient.deleteBeer(dto.getId());

        server.verify();
    }

    @Test
    void testDeleteNotFound() {
        server.expect(method(HttpMethod.DELETE))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH,
                        dto.getId()))
                //.andExpect(header("Authorization", "Basic dXNlcjpwYXNzd29yZA=="))
                .andExpect(header("Authorization", BEARER_TEST))
                .andRespond(withResourceNotFound());

        assertThrows(HttpClientErrorException.class, () -> {
            beerClient.deleteBeer(dto.getId());
        });

        server.verify();
    }

    private BeerDTO getBeerDto(){
        return BeerDTO.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("10.99"))
                .beerName("Mango Bobs")
                .beerStyle(BeerStyle.IPA)
                .quantityOnHand(500)
                .upc("123245")
                .build();
    }

    private BeerDTOPageImpl<BeerDTO> getPage(){
        return new BeerDTOPageImpl<>(Collections.singletonList(dto), 1, 25, 1);
    }

    private void mockGetByIdOperation() {
        server.expect(method(HttpMethod.GET))
                .andExpect(requestToUriTemplate(URL +
                        BeerClientImpl.GET_BEER_BY_ID_PATH, dto.getId()))
                //.andExpect(header("Authorization", "Basic dXNlcjpwYXNzd29yZA=="))
                .andExpect(header("Authorization", BEARER_TEST))
                .andRespond(withSuccess(dtoJson, MediaType.APPLICATION_JSON));
    }

}
