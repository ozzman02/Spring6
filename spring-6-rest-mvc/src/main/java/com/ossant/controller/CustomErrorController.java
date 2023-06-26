package com.ossant.controller;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class CustomErrorController {

    @ExceptionHandler
    ResponseEntity<?> handleJPAViolations(TransactionSystemException transactionSystemException) {
        ResponseEntity.BodyBuilder responseEntity = ResponseEntity.badRequest();
        if (transactionSystemException.getCause().getCause() instanceof ConstraintViolationException constraintViolationException) {
            List<Map<String, String>> errors =
                    constraintViolationException.getConstraintViolations().stream()
                            .map(constraintViolation -> {
                                Map<String, String> errorMap = new HashMap<>();
                                errorMap.put(constraintViolation.getPropertyPath().toString(), constraintViolation.getMessage());
                                return errorMap;
                            }).toList();
            return responseEntity.body(errors);
        }
        return responseEntity.build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> handleBindErrors(MethodArgumentNotValidException methodArgumentNotValidException) {
        List<Map<String, String>> errorList = methodArgumentNotValidException.getFieldErrors().stream()
                .map(fieldError -> {
                    Map<String, String> errorMap = new HashMap<>();
                    errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
                    return errorMap;
                }).toList();
        return ResponseEntity.badRequest().body(errorList);
    }
}
