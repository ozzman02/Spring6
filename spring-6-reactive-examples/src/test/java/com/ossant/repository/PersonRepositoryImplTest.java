package com.ossant.repository;

import com.ossant.domain.Person;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersonRepositoryImplTest {

    PersonRepository personRepository = new PersonRepositoryImpl();

    @Test
    void testGetByIdFound() {
        Mono<Person> personMono = personRepository.getById(3);

        //assertTrue(personMono.hasElement().block());
        assertEquals(Boolean.TRUE, personMono.hasElement().block());
    }

    /*
        In Reactive Programming we don't really want to use block so,
        StepVerifier is more elegant solution.
    */
    @Test
    void testGetByIdFoundStepVerifier() {
        Mono<Person> personMono = personRepository.getById(3);
        StepVerifier.create(personMono).expectNextCount(1).verifyComplete();
        personMono.subscribe(person -> {
            System.out.println(person.getFirstName());
        });
    }

    @Test
    void testGetByIdNotFound() {
        Mono<Person> personMono = personRepository.getById(6);

        //assertFalse(personMono.hasElement().block());
        assertNotEquals(Boolean.TRUE, personMono.hasElement().block());
    }

    @Test
    void testGetByIdNotFoundStepVerifier() {
        Mono<Person> personMono = personRepository.getById(6);
        StepVerifier.create(personMono).expectNextCount(0).verifyComplete();
        personMono.subscribe(person -> {
            System.out.println(person.getFirstName());
        });
    }

    @Test
    void testMonoByIdBlock() {
        Mono<Person> personMono = personRepository.getById(1);
        Person person = personMono.block(); // not preferred
        System.out.println(person);
    }

    @Test
    void testGetByIdSubscriber() {
        Mono<Person> personMono = personRepository.getById(1);
        /*
            personMono.subscribe(person -> {
                System.out.println(person.toString());
            });
         */
        personMono.subscribe(System.out::println);
    }

    @Test
    void testMapOperation() {
        Mono<Person> personMono = personRepository.getById(1);
        /*
            personMono.map(person -> {
                return person.getFirstName();
            }).subscribe(firstName -> {
                System.out.println(firstName);
            });
         */
        personMono.map(Person::getFirstName).subscribe(System.out::println);
    }

    @Test
    void testFluxBlockFirst() {
        Flux<Person> personFlux = personRepository.findAll();
        Person person = personFlux.blockFirst();
        System.out.println(person);
    }

    @Test
    void testFluxSubscriber() {
        Flux<Person> personFlux = personRepository.findAll();
        personFlux.subscribe(person -> {
            System.out.println(person.toString());
        });
    }

    @Test
    void testFluxMap() {
        Flux<Person> personFlux = personRepository.findAll();
        //personFlux.map(Person::getFirstName).subscribe(firstName -> System.out.println(firstName));
        personFlux.map(Person::getFirstName).subscribe(System.out::println);
    }

    @Test
    void testFluxToList() {
        Flux<Person> personFlux = personRepository.findAll();
        Mono<List<Person>> listMono = personFlux.collectList(); // mono with a list element inside
        listMono.subscribe(list -> {
            list.forEach(person -> System.out.println(person.getFirstName()));
        });
    }

    @Test
    void testFilterOnName() {
        personRepository.findAll()
                .filter(person -> person.getFirstName().equals("Fiona"))
                .subscribe(person -> System.out.println(person.getFirstName()));
    }

    @Test
    void testGetById() {
        Mono<Person> fionaMono = personRepository.findAll()
                .filter(person -> person.getFirstName().equals("Fiona"))
                .next();

        fionaMono.subscribe(person -> System.out.println(person.getFirstName()));
    }


    @Test
    void testFindPersonByIdNotFound() {
        Flux<Person> personFlux = personRepository.findAll();

        final Integer id = 8;

        /*
            Emit only the first item emitted by this Flux, into a new Mono.
            If called on an empty Flux, emits an empty Mono. If that happens we can see the error handling.

            Mono<Person> personMono = personFlux.filter(person -> person.getId() == id).next()

            We need to use single() since it expects and emit a single item from this Flux source or
            signal NoSuchElementException for an empty source, or IndexOutOfBoundsException for
            a source with more than one element.

            If we comment out the "subscribe" we will not have backpressure, no error handling statements
            are going to be shown.

        */
        Mono<Person> personMono = personFlux
                .filter(person -> person.getId() == id)
                .single()
                .doOnError(throwable -> {
                    System.out.println("Error occurred in the flux");
                    System.out.println(throwable.toString());
                });

        /*
            Subscriber is adding backpressure and it triggers the events.
            Without backpressure the code is not executed
        */
        personMono.subscribe(person -> {
            System.out.println(person.toString());
        }, throwable -> {
            System.out.println("Error occurred in the mono");
            System.out.println(throwable.toString());
        });
    }
}