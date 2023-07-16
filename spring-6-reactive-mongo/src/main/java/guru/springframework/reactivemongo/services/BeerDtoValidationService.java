package guru.springframework.reactivemongo.services;

import guru.springframework.reactivemongo.model.BeerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.server.ServerWebInputException;

import static guru.springframework.reactivemongo.app.ApplicationConstants.BEER_VALIDATION_OBJECT;

@Service
@RequiredArgsConstructor
public class BeerDtoValidationService {

    // We can use this validator service because we have spring-boot-starter-validation
    private final Validator validator;

    public void validate(BeerDTO beerDTO) {
        Errors errors = new BeanPropertyBindingResult(beerDTO, BEER_VALIDATION_OBJECT);
        validator.validate(beerDTO, errors);
        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());
        }
    }

}
