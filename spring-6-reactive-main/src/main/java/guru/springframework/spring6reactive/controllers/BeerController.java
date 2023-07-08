package guru.springframework.spring6reactive.controllers;

import guru.springframework.spring6reactive.model.BeerDTO;
import guru.springframework.spring6reactive.services.BeerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static guru.springframework.spring6reactive.app.ApplicationConstants.*;

@RestController
@RequiredArgsConstructor
public class BeerController {

    private final BeerService beerService;

    @GetMapping(BEER_PATH)
    Flux<BeerDTO> listBeers(){
        return beerService.listBeers();
    }

    @GetMapping(BEER_PATH_ID)
    Mono<BeerDTO> getBeerById(@PathVariable("beerId") Integer beerId) {
        return beerService.getBeerById(beerId);

    }

    // Mono<ResponseEntity<Void>>: Response will be empty mono without body
    @PostMapping(BEER_PATH)
    Mono<ResponseEntity<Void>> createNewBeer(@Validated @RequestBody BeerDTO beerDTO) {
        return beerService.saveNewBeer(beerDTO)
                .map(savedDto -> ResponseEntity.created(UriComponentsBuilder
                                .fromHttpUrl(BASE_URL + BEER_PATH + "/" + savedDto.getId())
                                .build().toUri())
                        .build());
    }

    @PutMapping(BEER_PATH_ID)
    Mono<ResponseEntity<Void>> updateExistingBeer(@PathVariable("beerId") Integer beerId,
                                                  @Validated @RequestBody BeerDTO beerDTO) {
        return beerService.updateBeer(beerId, beerDTO)
                .map(savedDto -> ResponseEntity.ok().build());
    }

    @PatchMapping(BEER_PATH_ID)
    Mono<ResponseEntity<Void>> patchExistingBeer(@PathVariable Integer beerId,
                                                 @Validated @RequestBody BeerDTO beerDTO) {
        return beerService.patchBeer(beerId, beerDTO)
                .map(updatedDto -> ResponseEntity.ok().build());
    }

    @DeleteMapping(BEER_PATH_ID)
    Mono<ResponseEntity<Void>> deleteById(@PathVariable Integer beerId) {
        return beerService.deleteBeerById(beerId)
                .map(response -> ResponseEntity.noContent().build());
    }

    /* Adds a Location header in the response
    @PostMapping(BEER_PATH)
    ResponseEntity<Void> createNewBeer(@RequestBody BeerDTO beerDTO) {
        AtomicInteger atomicInteger = new AtomicInteger();
        beerService.saveNewBeer(beerDTO).subscribe(savedBeerDto -> {
            atomicInteger.set(savedBeerDto.getId());
        });
        return ResponseEntity.created(UriComponentsBuilder
                        .fromHttpUrl(BASE_URL + BEER_PATH + "/" + atomicInteger.get())
                        .build().toUri())
                .build();

    }*/

}
