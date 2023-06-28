package com.ossant.services;

import com.ossant.entities.Beer;
import com.ossant.mappers.BeerMapper;
import com.ossant.model.BeerDTO;
import com.ossant.model.BeerStyle;
import com.ossant.repositories.BeerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Primary
@RequiredArgsConstructor
public class BeerServiceJPAImpl implements BeerService {

    private final BeerRepository beerRepository;

    private final BeerMapper beerMapper;

    @Override
    public List<BeerDTO> listBeers(String beerName, BeerStyle beerStyle, Boolean showInventory) {

        List<Beer> beerList;

        if (StringUtils.hasText(beerName) && beerStyle == null) {
            // beerList = beerRepository.findAll().stream().filter(beer -> beer.getBeerName().contains(beerName)).toList();
            beerList = beerRepository.findAllByBeerNameIsLikeIgnoreCase("%" + beerName + "%");
        } else if (!StringUtils.hasText(beerName) && beerStyle != null) {
            //beerList = beerRepository.findAll().stream().filter(beer -> beer.getBeerStyle().equals(beerStyle)).toList();
            beerList = beerRepository.findAllByBeerStyle(beerStyle);
        } else if (StringUtils.hasText(beerName) && beerStyle != null) {
            /*beerList = beerRepository.findAll().stream()
                    .filter(beer -> beer.getBeerName().contains(beerName) && beer.getBeerStyle().equals(beerStyle))
                    .collect(Collectors.toList());*/
            beerList = beerRepository.findAllByBeerNameIsLikeIgnoreCaseAndBeerStyle("%" + beerName + "%", beerStyle);
        } else {
            beerList = beerRepository.findAll();
        }

        if (showInventory != null && !showInventory) {
            beerList.forEach(beer -> beer.setQuantityOnHand(null));
        }

        return beerList.stream()
                .map(beerMapper::beerToBeerDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BeerDTO> getBeerById(UUID id) {
        return Optional.ofNullable(beerMapper.beerToBeerDto(beerRepository.findById(id).orElse(null)));
    }

    @Override
    public BeerDTO saveNewBeer(BeerDTO beerDTO) {
        return beerMapper.beerToBeerDto(beerRepository.save(beerMapper.beerDtoToBeer(beerDTO)));
    }

    @Override
    public Optional<BeerDTO> updateBeerById(UUID beerId, BeerDTO beerDTO) {
        // We can't do any updates outside the lambda function that's why we use AtomicReference
        AtomicReference<Optional<BeerDTO>> atomicReference = new AtomicReference<>();
        beerRepository.findById(beerId).ifPresentOrElse(foundBeer -> {
            foundBeer.setBeerName(beerDTO.getBeerName());
            foundBeer.setBeerStyle(beerDTO.getBeerStyle());
            foundBeer.setUpc(beerDTO.getUpc());
            foundBeer.setPrice(beerDTO.getPrice());
            foundBeer.setUpdateDate(LocalDateTime.now());
            atomicReference.set(Optional.of(beerMapper.beerToBeerDto(foundBeer)));
            beerRepository.save(foundBeer);
        }, () -> {
            atomicReference.set(Optional.empty());
        });
        return atomicReference.get();
    }

    @Override
    public Boolean deleteById(UUID beerId) {
        if (beerRepository.existsById(beerId)) {
            beerRepository.deleteById(beerId);
            return true;
        }
        return false;
    }

    @Override
    public Optional<BeerDTO> patchBeerById(UUID beerId, BeerDTO beerDTO) {
        AtomicReference<Optional<BeerDTO>> atomicReference = new AtomicReference<>();
        beerRepository.findById(beerId).ifPresentOrElse(foundBeer -> {
            if (StringUtils.hasText(beerDTO.getBeerName())) foundBeer.setBeerName(beerDTO.getBeerName());
            if (beerDTO.getBeerStyle() != null) foundBeer.setBeerStyle(beerDTO.getBeerStyle());
            if (beerDTO.getPrice() != null) foundBeer.setPrice(beerDTO.getPrice());
            if (beerDTO.getQuantityOnHand() != null) foundBeer.setQuantityOnHand(beerDTO.getQuantityOnHand());
            if (StringUtils.hasText(beerDTO.getUpc())) foundBeer.setUpc(beerDTO.getUpc());
            atomicReference.set(Optional.of(beerMapper.beerToBeerDto(beerRepository.save(foundBeer))));
        }, () -> {
            atomicReference.set(Optional.empty());
        });
        return atomicReference.get();
    }

}
