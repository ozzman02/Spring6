package com.ossant.services;

import com.ossant.entities.Beer;
import com.ossant.mappers.BeerMapper;
import com.ossant.model.BeerDTO;
import com.ossant.model.BeerStyle;
import com.ossant.repositories.BeerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Primary
@RequiredArgsConstructor
public class BeerServiceJPAImpl implements BeerService {

    private final BeerRepository beerRepository;

    private final BeerMapper beerMapper;

    private final static int DEFAULT_PAGE = 0;

    private final static int DEFAULT_PAGE_SIZE = 25;

    @Override
    public Page<BeerDTO> listBeers(String beerName,
                                   BeerStyle beerStyle,
                                   Boolean showInventory,
                                   Integer pageNumber,
                                   Integer pageSize) {
        Page<Beer> beerPage;

        PageRequest pageRequest = buildPageRequest(pageNumber, pageSize);

        if (StringUtils.hasText(beerName) && beerStyle == null) {
            // beerList = beerRepository.findAll().stream().filter(beer -> beer.getBeerName().contains(beerName)).toList();
            beerPage = beerRepository.findAllByBeerNameIsLikeIgnoreCase("%" + beerName + "%", pageRequest);
        } else if (!StringUtils.hasText(beerName) && beerStyle != null) {
            //beerList = beerRepository.findAll().stream().filter(beer -> beer.getBeerStyle().equals(beerStyle)).toList();
            beerPage = beerRepository.findAllByBeerStyle(beerStyle, pageRequest);
        } else if (StringUtils.hasText(beerName) && beerStyle != null) {
            /*beerList = beerRepository.findAll().stream()
                    .filter(beer -> beer.getBeerName().contains(beerName) && beer.getBeerStyle().equals(beerStyle))
                    .collect(Collectors.toList());*/
            beerPage = beerRepository.findAllByBeerNameIsLikeIgnoreCaseAndBeerStyle(
                    "%" + beerName + "%", beerStyle, pageRequest);
        } else {
            beerPage = beerRepository.findAll(pageRequest);
        }

        if (showInventory != null && !showInventory) {
            beerPage.forEach(beer -> beer.setQuantityOnHand(null));
        }
        //return beerPage.stream().map(beerMapper::beerToBeerDto).collect(Collectors.toList());
        return beerPage.map(beerMapper::beerToBeerDto);
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

    private PageRequest buildPageRequest(Integer pageNumber, Integer pageSize) {
        int queryPageNumber = pageNumber != null && pageNumber > 0 ? pageNumber - 1 : DEFAULT_PAGE;
        int queryPageSize = pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
        Sort sort = Sort.by(Sort.Order.asc("beerName"));
        return PageRequest.of(queryPageNumber, queryPageSize, sort);
    }
}
