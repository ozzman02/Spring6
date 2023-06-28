package com.ossant.repositories;

import com.ossant.entities.Beer;
import com.ossant.model.BeerStyle;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("local-mysql")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@Import({BootstrapData.class, BeerCsvServiceImpl.class})
class BeerRepositoryTest {

    @Autowired
    BeerRepository beerRepository;

    @Test
    void testSaveBeer() {
        Beer savedBeer = beerRepository.save(Beer.builder()
                .beerName("MyBeer")
                .beerStyle(BeerStyle.PALE_ALE)
                .upc("2222")
                .price(new BigDecimal("11.99"))
                .build());

        // Test is running very fast, and it's passing because data is not being flushed to the db.
        // To see the constraint validation error you need to explicitly flush data.
        beerRepository.flush();

        assertThat(savedBeer).isNotNull();
        assertThat(savedBeer.getId()).isNotNull();
    }

    // Need to annotate the entity with @Size annotation otherwise we will have DataIntegrityViolationException
    @Test
    void testSaveBeerNameTooLong() {
        assertThrows(ConstraintViolationException.class, () -> {
            Beer savedBeer = beerRepository.save(Beer.builder()
                    .beerName("MyBeerMyBeerMyBeerMyBeerMyBeerMyBeerMyBeerMyBeerMyBeerMyBeerMyBeer")
                    .beerStyle(BeerStyle.PALE_ALE)
                    .upc("2222")
                    .price(new BigDecimal("11.99"))
                    .build());

            beerRepository.flush();
        });
    }

    @Test
    void testGetBeerListByName() {
        List<Beer> list = beerRepository.findAllByBeerNameIsLikeIgnoreCase("%IPA%");
        assertThat(list.size()).isEqualTo(336);
    }

    @Test
    void testGetBeerListByBeerStyle() {
        List<Beer> list = beerRepository.findAllByBeerStyle(BeerStyle.IPA);
        assertThat(list.size()).isEqualTo(548);
    }

    @Test
    void testGetBeerListByBeerNameAndBeerStyle() {
        List<Beer> list = beerRepository
                .findAllByBeerNameIsLikeIgnoreCaseAndBeerStyle("%IPA%", BeerStyle.IPA);
        assertThat(list.size()).isEqualTo(310);
    }

}