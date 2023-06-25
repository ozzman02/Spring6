package com.ossant.controller;

import com.ossant.model.BeerDTO;
import com.ossant.services.BeerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
//@RequestMapping("/api/v1/beer")
public class BeerController {

    public static final String BEER_PATH = "/api/v1/beer";

    public static final String BEER_PATH_ID = BEER_PATH + "/{beerId}";

    private final BeerService beerService;

    @GetMapping(BEER_PATH)
    public List<BeerDTO> listBeers(){
        return beerService.listBeers();
    }

    @GetMapping(BEER_PATH_ID)
    public BeerDTO getBeerById(@PathVariable("beerId") UUID beerId){
        log.debug("Get Beer by Id - in controller");
        return beerService.getBeerById(beerId).orElseThrow(NotFoundException::new);
    }

    @PostMapping(BEER_PATH)
    //@RequestMapping(value = BEER_PATH, method = RequestMethod.POST)
    public ResponseEntity<?> handlePost(@RequestBody BeerDTO beerDTO) {
        BeerDTO savedBeerDTO = beerService.saveNewBeer(beerDTO);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/api/v1/beer/" + savedBeerDTO.getId().toString());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @PutMapping(BEER_PATH_ID)
    public ResponseEntity<?> updateById(@PathVariable("beerId")UUID beerId, @RequestBody BeerDTO beerDTO){
        if (beerService.updateBeerById(beerId, beerDTO).isEmpty()) {
            throw new NotFoundException();
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(BEER_PATH_ID)
    public ResponseEntity<?> deleteById(@PathVariable("beerId") UUID beerId){
        if (!beerService.deleteById(beerId)) {
            throw new NotFoundException();
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping(BEER_PATH_ID)
    public ResponseEntity<?> updateBeerPatchById(@PathVariable("beerId")UUID beerId, @RequestBody BeerDTO beerDTO){
        if (!beerService.patchBeerById(beerId, beerDTO)) throw new NotFoundException();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }









}
