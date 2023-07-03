package com.ossant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ossant.configuration.SpringSecurityConfig;
import com.ossant.model.BeerDTO;
import com.ossant.services.BeerService;
import com.ossant.services.BeerServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.ossant.controller.BeerController.BEER_PATH;
import static com.ossant.controller.BeerController.BEER_PATH_ID;
import static com.ossant.controller.CustomerControllerTest.jwtRequestPostProcessor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@WebMvcTest(BeerController.class)
@Import(SpringSecurityConfig.class)
@ActiveProfiles("local-mysql")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BeerControllerTest {

    //public static final String USERNAME = "user";

    //public static final String PASSWORD = "password";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    BeerService beerService;

    @Captor
    ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Captor
    ArgumentCaptor<BeerDTO> beerArgumentCaptor;

    BeerServiceImpl beerServiceImpl;

    @BeforeEach
    void setup() {
        beerServiceImpl = new BeerServiceImpl();
    }

    /*
        1. Without objectMapper.findAndRegisterModules(); we can get an error like this one:
        com.fasterxml.jackson.databind.exc.InvalidDefinitionException: Java 8 date/time type `java.time.LocalDateTime` not supported by default: add Module "com.fasterxml.jackson.datatype:jackson-datatype-jsr310" to enable handling (through reference chain: com.ossant.model.Beer["createdDate"])

        Is better to autowired the ObjectMapper.

            @Test
            void testCreateNewBeer() throws JsonProcessingException {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.findAndRegisterModules();
                Beer beer = beerServiceImpl.listBeers().get(0);
                System.out.println(objectMapper.writeValueAsString(beer));
            }

        2. Default Spring Security only allows to test security with GET methods.

        3. When Basic Spring Security is removed we need to refactor the test in order to use a valid jwt.
            Now we have an Authorization Server and a Resource Server.

    */

    @Test
    void testListBeers() throws Exception {
        given(beerService.listBeers(any(), any(), any(), any(), any()))
                .willReturn(beerServiceImpl.listBeers(null, null, false, null, null));
        mockMvc.perform(get(BEER_PATH)
                        //.with(httpBasic(USERNAME, PASSWORD))
                        .with(jwtRequestPostProcessor)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()", is(3)));
    }


    @Test
    void testEmptyListBeers() throws Exception {
        given(beerService.listBeers(any(), any(), any(), any(), any())).willReturn(Page.empty());
        mockMvc.perform(get(BEER_PATH)
                        //.with(httpBasic(USERNAME, PASSWORD))
                        .with(jwtRequestPostProcessor)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()", is(0)));
    }

    @Test
    void testGetBeerById() throws Exception {
        BeerDTO testBeerDTO = beerServiceImpl.listBeers(null, null, null, 1, 25)
                .getContent().get(0);

        //given(beerService.getBeerById(any(UUID.class))).willReturn(testBeer);
        given(beerService.getBeerById(testBeerDTO.getId())).willReturn(Optional.of(testBeerDTO));

        mockMvc.perform(get(BEER_PATH_ID, testBeerDTO.getId())
                        //.with(httpBasic(USERNAME, PASSWORD))
                        .with(jwtRequestPostProcessor)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testBeerDTO.getId().toString())))
                .andExpect(jsonPath("$.beerName", is(testBeerDTO.getBeerName())));
    }

    @Test
    void testGetBeerByIdNotFound() throws Exception {
        given(beerService.getBeerById(any(UUID.class))).willReturn(Optional.empty());
        mockMvc.perform(get(BEER_PATH_ID, UUID.randomUUID())
                        //.with(httpBasic(USERNAME, PASSWORD)))
                .with(jwtRequestPostProcessor))
                .andExpect(status().isNotFound());
    }

    /*
        In order to debug go to application.properties and add logging.level.org.springframework.security=trace

        Error after implementing basic security:

            Invalid CSRF token found for http://localhost/api/v1/beer
            Responding with 403 status code
            Not injecting HSTS header since it did not match request to [Is Secure]

        Solution:

            1. Create a Security Configuration class and disable csrf:

                @Bean
                public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                    http
                        .authorizeHttpRequests(authConfig -> {
                            authConfig.anyRequest().authenticated();
                        })
                        .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                        .httpBasic(Customizer.withDefaults());
                    return http.build();
                }

            2. @Import(SpringSecurityConfig.class)

    */
    @Test
    void testSaveNewBeer() throws Exception {
        BeerDTO beerDTO = beerServiceImpl.listBeers(null, null, null, 1, 25)
                .getContent().get(0);
        beerDTO.setVersion(null);
        beerDTO.setId(null);
        given(beerService.saveNewBeer(any(BeerDTO.class)))
                .willReturn(beerServiceImpl.listBeers(null, null, null, 1, 25)
                        .getContent().get(1));
        mockMvc.perform(post(BEER_PATH)
                        //.with(httpBasic(USERNAME, PASSWORD))
                        .with(jwtRequestPostProcessor)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    void testUpdateBeerById() throws Exception {
        BeerDTO beerDTO = beerServiceImpl.listBeers(null, null, null, 1, 25)
                .getContent().get(0);
        given(beerService.updateBeerById(any(), any())).willReturn(Optional.of(beerDTO));
        mockMvc.perform(put(BEER_PATH_ID, beerDTO.getId())
                        //.with(httpBasic(USERNAME, PASSWORD))
                        .with(jwtRequestPostProcessor)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerDTO)))
                .andExpect(status().isNoContent());
        verify(beerService).updateBeerById(any(UUID.class), any(BeerDTO.class));
    }

    @Test
    void testUpdateBeerByIdNotFound() throws Exception {
        BeerDTO beerDTO = beerServiceImpl.listBeers(null, null, null, 1, 25)
                .getContent().get(0);
        given(beerService.updateBeerById(any(), any())).willReturn(Optional.empty());
        mockMvc.perform(put(BEER_PATH_ID, beerDTO.getId())
                        //.with(httpBasic(USERNAME, PASSWORD))
                        .with(jwtRequestPostProcessor)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerDTO)))
                .andExpect(status().isNotFound());
        verify(beerService).updateBeerById(any(UUID.class), any(BeerDTO.class));
    }

    @Test
    void testUpdateBeerByIdBlankName() throws Exception {
        BeerDTO beerDTO = beerServiceImpl.listBeers(null, null, null, 1, 25)
                .getContent().get(0);
        beerDTO.setBeerName("");
        given(beerService.updateBeerById(any(), any())).willReturn(Optional.of(beerDTO));
        mockMvc.perform(put(BEER_PATH_ID, beerDTO.getId())
                        //.with(httpBasic(USERNAME, PASSWORD))
                        .with(jwtRequestPostProcessor)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    @Test
    void testDeleteBeerById() throws Exception {
        BeerDTO beerDTO = beerServiceImpl.listBeers(null, null, null, 1, 25)
                .getContent().get(0);
        given(beerService.deleteById(any())).willReturn(true);
        mockMvc.perform(delete(BEER_PATH_ID, beerDTO.getId())
                        //.with(httpBasic(USERNAME, PASSWORD))
                        .with(jwtRequestPostProcessor)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        //ArgumentCaptor<UUID> uuidArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(beerService).deleteById(uuidArgumentCaptor.capture());
        assertThat(beerDTO.getId()).isEqualTo(uuidArgumentCaptor.getValue());
    }

    @Test
    void testByDeleteBeerByIdNotFound() throws Exception {
        BeerDTO beerDTO = beerServiceImpl.listBeers(null, null, null, 1, 25)
                .getContent().get(0);
        given(beerService.deleteById(any())).willReturn(false);
        mockMvc.perform(delete(BEER_PATH_ID, beerDTO.getId())
                        //.with(httpBasic(USERNAME, PASSWORD))
                        .with(jwtRequestPostProcessor)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(beerService).deleteById(uuidArgumentCaptor.capture());
        assertThat(beerDTO.getId()).isEqualTo(uuidArgumentCaptor.getValue());
    }

    @Test
    void testUpdateBeerPatchById() throws Exception {
        BeerDTO beerDTO = beerServiceImpl.listBeers(null, null, null, 1, 25)
                .getContent().get(0);
        Map<String, Object> beerMap = new HashMap<>();
        beerMap.put("beerName", "New Name");
        given(beerService.patchBeerById(any(), any())).willReturn(Optional.of(beerDTO));
        mockMvc.perform(patch(BEER_PATH_ID, beerDTO.getId())
                        //.with(httpBasic(USERNAME, PASSWORD))
                        .with(jwtRequestPostProcessor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerMap)))
                .andExpect(status().isNoContent());

        verify(beerService).patchBeerById(uuidArgumentCaptor.capture(), beerArgumentCaptor.capture());

        assertThat(beerDTO.getId()).isEqualTo(uuidArgumentCaptor.getValue());
        assertThat(beerMap.get("beerName")).isEqualTo(beerArgumentCaptor.getValue().getBeerName());
    }

    @Test
    void testCreateBeerNullBeerName() throws Exception {
        BeerDTO beerDTO = BeerDTO.builder().build();
        given(beerService.saveNewBeer(any(BeerDTO.class)))
                .willReturn(beerServiceImpl.listBeers(null, null, null, 1, 25)
                        .getContent().get(1));
        MvcResult mvcResult = mockMvc.perform(post(BeerController.BEER_PATH)
                        //.with(httpBasic(USERNAME, PASSWORD))
                        .with(jwtRequestPostProcessor)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(beerDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()", is(6))) // list of two errors
                .andReturn();
        System.out.println(mvcResult.getResponse().getContentAsString());
    }

}