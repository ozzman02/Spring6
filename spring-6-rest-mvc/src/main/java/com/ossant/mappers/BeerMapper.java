package com.ossant.mappers;

import com.ossant.entities.Beer;
import com.ossant.model.BeerDTO;
import org.mapstruct.Mapper;

@Mapper
public interface BeerMapper {

    Beer beerDtoToBeer(BeerDTO beerDTO);

    BeerDTO beerToBeerDto(Beer beer);

}
