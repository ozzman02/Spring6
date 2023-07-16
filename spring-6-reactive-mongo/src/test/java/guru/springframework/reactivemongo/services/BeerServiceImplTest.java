package guru.springframework.reactivemongo.services;

import guru.springframework.reactivemongo.domain.Beer;
import guru.springframework.reactivemongo.mappers.BeerMapper;
import guru.springframework.reactivemongo.mappers.BeerMapperImpl;
import guru.springframework.reactivemongo.model.BeerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static guru.springframework.reactivemongo.app.ApplicationConstants.TEST_NEW_BEER_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
public class BeerServiceImplTest {

    @Autowired
    BeerService beerService;

    @Autowired
    BeerMapper beerMapper;

    BeerDTO beerDTO;

    @BeforeEach
    void setUp() {
        beerDTO = beerMapper.beerToBeerDto(getTestBeer());
    }

    /*
        We are adding the sleep because we are not seeing the id in the console.
        The test is completing before the subscriber completes, so before everything goes through.
        So the JVM is terminating before the subscription completes because it's being done in a different
        thread.

        We need to use awaitility instead of using sleep method.

    @Test
    void saveBeerTest() throws InterruptedException {
        Mono<BeerDTO> savedMono = beerService.saveBeer(Mono.just(beerDTO));
        savedMono.subscribe(savedDto -> {
            System.out.println(savedDto.getId());
        });
        Thread.sleep(1000L);
    } */

    /*@Test
    void saveBeer() {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);

        Mono<BeerDTO> savedMono = beerService.saveBeer(Mono.just(beerDTO));
        savedMono.subscribe(savedDto -> {
            System.out.println(savedDto.getId());
            atomicBoolean.set(true);
        });

        await().untilTrue(atomicBoolean);
    }*/

    /*
        We are requiring the test to wait for that subscribe feature to complete.

        Now, one thing that happens here is if you do an assertion inside the subscribe, it's going to get
        suppressed.

        So the exception from that assertion, if it fails, it gets suppressed and your test passes.
        So not a desirable feature for a test.

        So that's why we are using the atomic reference to get a reference to that persisted object.
    */
    @Test
    @DisplayName("Test Save Beer Using Subscriber")
    void saveBeerUseSubscriber() {

        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        AtomicReference<BeerDTO> atomicDto = new AtomicReference<>();

        Mono<BeerDTO> savedMono = beerService.saveBeer(Mono.just(beerDTO));

        savedMono.subscribe(savedDto -> {
            System.out.println(savedDto.getId());
            atomicBoolean.set(true);
            atomicDto.set(savedDto);
        });

        await().untilTrue(atomicBoolean);

        BeerDTO persistedDto = atomicDto.get();
        assertThat(persistedDto).isNotNull();
        assertThat(persistedDto.getId()).isNotNull();
    }

    @Test
    @DisplayName("Test Save Beer Using Block")
    void testSaveBeerUseBlock() {
        BeerDTO savedDto = beerService.saveBeer(Mono.just(getTestBeerDto())).block();
        assertThat(savedDto).isNotNull();
        assertThat(savedDto.getId()).isNotNull();
    }

    @Test
    @DisplayName("Test Update Beer Using Block")
    void testUpdateBlocking() {
        BeerDTO savedBeerDto = getSavedBeerDto();
        savedBeerDto.setBeerName(TEST_NEW_BEER_NAME);

        BeerDTO updatedDto = beerService.saveBeer(Mono.just(savedBeerDto)).block();

        //verify exists in db
        assert updatedDto != null;
        BeerDTO fetchedDto = beerService.getById(updatedDto.getId()).block();
        assert fetchedDto != null;
        assertThat(fetchedDto.getBeerName()).isEqualTo(TEST_NEW_BEER_NAME);
    }

    @Test
    @DisplayName("Test Update Using Reactive Streams")
    void testUpdateStreaming() {
        AtomicReference<BeerDTO> atomicDto = new AtomicReference<>();

        /*
            beerService.saveBeer(Mono.just(getTestBeerDto()))
                .map(savedBeerDto -> {
                    savedBeerDto.setBeerName(newName);
                    return savedBeerDto;
                })
                .flatMap(beerService::saveBeer) // save updated beer
                .flatMap(savedUpdatedDto -> beerService.getById(savedUpdatedDto.getId())) // get from db
                .subscribe(dtoFromDb -> {
                    atomicDto.set(dtoFromDb);
                });
        */

        beerService.saveBeer(Mono.just(getTestBeerDto()))
                .map(savedBeerDto -> {
                    savedBeerDto.setBeerName(TEST_NEW_BEER_NAME);
                    return savedBeerDto;
                })
                .flatMap(beerService::saveBeer) // save updated beer
                .flatMap(savedUpdatedDto -> beerService.getById(savedUpdatedDto.getId())) // get from db
                .subscribe(atomicDto::set);

        await().until(() -> atomicDto.get() != null);
        assertThat(atomicDto.get().getBeerName()).isEqualTo(TEST_NEW_BEER_NAME);
    }

    /*
        We're working with a persistent database in this example. So we are going to save it.
        We're creating an object to delete.
    */
    @Test
    void testDeleteBeer() {
        BeerDTO beerToDelete = getSavedBeerDto();

        /* After using block() the expected value is null */
        beerService.deleteBeerById(beerToDelete.getId()).block();

        Mono<BeerDTO> expectedEmptyBeerMono = beerService.getById(beerToDelete.getId());

        BeerDTO emptyBeer = expectedEmptyBeerMono.block();

        assertThat(emptyBeer).isNull();

    }

    @Test
    void testFindFirstByBeerName() {
        BeerDTO beerDto = getSavedBeerDto();
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        Mono<BeerDTO> foundDto = beerService.findFirstByBeerName(beerDto.getBeerName());
        foundDto.subscribe(dto -> {
            System.out.println(dto.toString());
            atomicBoolean.set(true);
        });
        await().untilTrue(atomicBoolean);
    }

    @Test
    void testFindByBeerStyle() {
        BeerDTO beerDto = getSavedBeerDto();
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        beerService.findByBeerStyle(beerDto.getBeerStyle())
                .subscribe(dto -> {
                    System.out.println(dto.toString());
                    atomicBoolean.set(true);
                });
        await().untilTrue(atomicBoolean);
    }

    @Test
    void testPatchBeerStreaming() {
        AtomicReference<BeerDTO> atomicDto = new AtomicReference<>();
        BeerDTO savedBeerDto = getSavedBeerDto();
        savedBeerDto.setBeerName(TEST_NEW_BEER_NAME);
        beerService.patchBeer(savedBeerDto.getId(), savedBeerDto)
                .flatMap(patchedBeerDto ->
                        beerService.getById(patchedBeerDto.getId()))
                .subscribe(atomicDto::set);
        await().until(() -> atomicDto.get() != null);
        assertThat(atomicDto.get().getBeerName()).isEqualTo(TEST_NEW_BEER_NAME);
    }

    /* For our test we have a pattern where we need to create a saved object in the database */
    public BeerDTO getSavedBeerDto() {
        return beerService.saveBeer(Mono.just(getTestBeerDto())).block();
    }

    public static BeerDTO getTestBeerDto(){
        return new BeerMapperImpl().beerToBeerDto(getTestBeer());
    }

    public static Beer getTestBeer() {
        return Beer.builder()
                .beerName("Space Dust")
                .beerStyle("IPA")
                .price(BigDecimal.TEN)
                .quantityOnHand(12)
                .upc("123213")
                .build();
    }

}