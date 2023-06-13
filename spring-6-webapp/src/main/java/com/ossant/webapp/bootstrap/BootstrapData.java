package com.ossant.webapp.bootstrap;

import com.ossant.webapp.domain.Author;
import com.ossant.webapp.domain.Book;
import com.ossant.webapp.domain.Publisher;
import com.ossant.webapp.repositories.AuthorRepository;
import com.ossant.webapp.repositories.BookRepository;
import com.ossant.webapp.repositories.PublisherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class BootstrapData implements CommandLineRunner {

    Logger logger = LoggerFactory.getLogger(BootstrapData.class);

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final PublisherRepository publisherRepository;

    public BootstrapData(AuthorRepository authorRepository,
                         BookRepository bookRepository,
                         PublisherRepository publisherRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.publisherRepository = publisherRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        Author author1 = new Author();
        author1.setFirstName("Eric");
        author1.setLastName("Evans");

        Book book1 = new Book();
        book1.setTitle("Domain Driven Design");
        book1.setIsbn("1234567");

        Author author1Saved = authorRepository.save(author1);
        Book book1Saved = bookRepository.save(book1);

        Author author2 = new Author();
        author2.setFirstName("Rod");
        author2.setLastName("Johnson");

        Book book2 = new Book();
        book2.setTitle("J2EE Development without EJB");
        book2.setIsbn("54757585");

        Author author2Saved = authorRepository.save(author2);
        Book book2Saved = bookRepository.save(book2);

        author1Saved.getBooks().add(book1Saved);
        author2Saved.getBooks().add(book2Saved);
        book1Saved.getAuthors().add(author1Saved);
        book2Saved.getAuthors().add(author2Saved);

        Publisher publisher1 = new Publisher();
        publisher1.setPublisherName("Publisher1");
        publisher1.setAddress("Address1");
        publisher1.setCity("City1");
        publisher1.setState("State1");
        publisher1.setZipCode("zip1");

        Publisher publisher1Saved = publisherRepository.save(publisher1);
        book1Saved.setPublisher(publisher1Saved);
        book2Saved.setPublisher(publisher1Saved);

        authorRepository.saveAll(Set.of(author1Saved, author2Saved));
        bookRepository.saveAll(Set.of(book1Saved, book2Saved));

        //System.out.println("In Bootstrap");
        logger.info("In Bootstrap");
        //System.out.println("Author count: " + authorRepository.count());
        logger.info("Author count: {}", authorRepository.count());
        //System.out.println("Book count: " + bookRepository.count());
        logger.info("Book count: {}", bookRepository.count());
        //System.out.println("Publisher count: " + publisherRepository.count());
        logger.info("Publisher count: {}", publisherRepository.count());

    }

}
