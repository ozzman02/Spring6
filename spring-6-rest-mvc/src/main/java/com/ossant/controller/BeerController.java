package com.ossant.controller;

import com.ossant.model.Beer;
import com.ossant.services.BeerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Slf4j
@AllArgsConstructor
@Controller
public class BeerController {

    private final BeerService beerService;

    public Beer getBeerById(UUID id) {
        log.debug("Get Beer By Id in controller was called");
        return beerService.getBeerById(id);
    }


}
