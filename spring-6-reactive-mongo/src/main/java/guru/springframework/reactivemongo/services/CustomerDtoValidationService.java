package guru.springframework.reactivemongo.services;

import guru.springframework.reactivemongo.model.CustomerDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.server.ServerWebInputException;

import static guru.springframework.reactivemongo.app.ApplicationConstants.CUSTOMER_VALIDATION_OBJECT;

@Service
@RequiredArgsConstructor
public class CustomerDtoValidationService {

    private final Validator validator;

    public void validate(CustomerDTO customerDTO) {
        Errors errors = new BeanPropertyBindingResult(customerDTO, CUSTOMER_VALIDATION_OBJECT);
        validator.validate(customerDTO, errors);
        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());
        }
    }

}
