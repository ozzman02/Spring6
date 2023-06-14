package com.ossant.di.services;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class GreetingServicePrimary implements GreetingService {
    @Override
    public String sayGreeting() {
        return "Hello from the primary bean";
    }
}
