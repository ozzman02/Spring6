package guru.springframework.reactivemongo.web.fn;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static guru.springframework.reactivemongo.app.ApplicationConstants.BEER_PATH;
import static guru.springframework.reactivemongo.app.ApplicationConstants.BEER_PATH_ID;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class BeerRouterConfig {

    private final BeerHandler handler;

    @Bean
    public RouterFunction<ServerResponse> beerRoutes() {
        return route()
                .GET(BEER_PATH, accept(APPLICATION_JSON), handler::listBeers)
                .GET(BEER_PATH_ID, accept(APPLICATION_JSON), handler::getBeerById)
                .POST(BEER_PATH, accept(APPLICATION_JSON), handler::createNewBeer)
                .PUT(BEER_PATH_ID, accept(APPLICATION_JSON), handler::updateBeerById)
                .PATCH(BEER_PATH_ID, accept(APPLICATION_JSON), handler::patchBeerById)
                .DELETE(BEER_PATH_ID, accept(APPLICATION_JSON), handler::deleteBeerById)
                .build();
    }
}
