package com.ossant.services;

import com.ossant.model.Beer;

import java.util.UUID;

public interface BeerService {

    Beer getBeerById(UUID id);

}
