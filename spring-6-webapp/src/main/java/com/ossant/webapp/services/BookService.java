package com.ossant.webapp.services;

import com.ossant.webapp.domain.Book;

public interface BookService {

    Iterable<Book> findAll();

}
