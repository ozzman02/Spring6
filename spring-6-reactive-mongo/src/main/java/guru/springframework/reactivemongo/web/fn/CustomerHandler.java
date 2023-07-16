package guru.springframework.reactivemongo.web.fn;

import guru.springframework.reactivemongo.model.CustomerDTO;
import guru.springframework.reactivemongo.services.CustomerDtoValidationService;
import guru.springframework.reactivemongo.services.CustomerService;
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
public class CustomerHandler {

    private final CustomerService customerService;

    private final CustomerDtoValidationService validationService;

    public Mono<ServerResponse> listCustomers(ServerRequest serverRequest) {
        Flux<CustomerDTO> flux;
        if (serverRequest.queryParam(CUSTOMER_NAME_QUERY_PARAM_NAME).isPresent()) {
            flux = customerService.findByCustomerName(serverRequest.queryParam(CUSTOMER_NAME_QUERY_PARAM_NAME).get());
        } else {
            flux = customerService.listCustomers();
        }
        return ServerResponse.ok().body(flux, CustomerDTO.class);
    }

    public Mono<ServerResponse> getCustomerById(ServerRequest request) {
        return ServerResponse
                .ok().body(customerService.getCustomerById(request.pathVariable(CUSTOMER_ID))
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND))), CustomerDTO.class);
    }

    public Mono<ServerResponse> createNewCustomer(ServerRequest request) {
        return customerService.saveCustomer(request.bodyToMono(CustomerDTO.class)
                        .doOnNext(validationService::validate))
                .flatMap(customerDTO -> ServerResponse.created(UriComponentsBuilder
                                .fromPath(CUSTOMER_PATH_ID)
                                .build(customerDTO.getId()))
                        .build());
    }

    public Mono<ServerResponse> updateCustomer(ServerRequest request) {
        return request.bodyToMono(CustomerDTO.class)
                .doOnNext(validationService::validate)
                .flatMap(customerDTO -> customerService.updateCustomer(request.pathVariable(CUSTOMER_ID), customerDTO))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(updatedDto -> ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> patchCustomer(ServerRequest request) {
        return request.bodyToMono(CustomerDTO.class)
                .doOnNext(validationService::validate)
                .flatMap(customerDTO -> customerService.patchCustomer(request.pathVariable(CUSTOMER_ID), customerDTO))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(patchedDto -> ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> deleteCustomer(ServerRequest request){
        return customerService.getCustomerById(request.pathVariable(CUSTOMER_ID))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .flatMap(customerDTO -> customerService.deleteCustomer(customerDTO.getId()))
                .then(ServerResponse.noContent().build());
    }

}
