package guru.springframework.reactivemongo.web.fn;

import guru.springframework.reactivemongo.model.BeerDTO;
import guru.springframework.reactivemongo.services.BeerDtoValidationService;
import guru.springframework.reactivemongo.services.BeerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static guru.springframework.reactivemongo.app.ApplicationConstants.*;

@Component
@RequiredArgsConstructor
public class BeerHandler {

    private final BeerService beerService;

    private final BeerDtoValidationService validationService;

    public Mono<ServerResponse> listBeers(ServerRequest request) {
        Flux<BeerDTO> flux;
        if (request.queryParam(BEER_STYLE_QUERY_PARAM_NAME).isPresent()) {
            flux = beerService.findByBeerStyle(request.queryParam(BEER_STYLE_QUERY_PARAM_NAME).get());
        } else {
            flux = beerService.listBeers();
        }
        return ServerResponse.ok().body(flux, BeerDTO.class);
    }

    public Mono<ServerResponse> getBeerById(ServerRequest request) {
        return ServerResponse
                .ok().body(beerService.getById(request.pathVariable(BEER_ID))
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND))), BeerDTO.class);
    }

    public Mono<ServerResponse> createNewBeer(ServerRequest request) {
        return beerService.saveBeer(request.bodyToMono(BeerDTO.class)
                        .doOnNext(validationService::validate))
                .flatMap(beerDTO -> ServerResponse.created(UriComponentsBuilder
                                .fromPath(BEER_PATH_ID)
                                .build(beerDTO.getId()))
                        .build());
    }

    public Mono<ServerResponse> updateBeerById(ServerRequest request) {
        return request.bodyToMono(BeerDTO.class)
                .doOnNext(validationService::validate)
                .flatMap(beerDTO -> beerService.updateBeer(request.pathVariable(BEER_ID), beerDTO))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(updatedDto -> ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> patchBeerById(ServerRequest request) {
        return request.bodyToMono(BeerDTO.class)
                .doOnNext(validationService::validate)
                .flatMap(beerDTO -> beerService.patchBeer(request.pathVariable(BEER_ID), beerDTO))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(patchedDto -> ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> deleteBeerById(ServerRequest request){
        return beerService.getById(request.pathVariable(BEER_ID))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(beerDTO -> beerService.deleteBeerById(beerDTO.getId()))
                .then(ServerResponse.noContent().build());
    }

}
