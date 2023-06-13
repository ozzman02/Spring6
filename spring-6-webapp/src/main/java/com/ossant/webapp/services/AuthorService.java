package com.ossant.webapp.services;

import com.ossant.webapp.domain.Author;

public interface AuthorService {

    Iterable<Author> findAll();
}
