package com.ossant.di.services;

import org.springframework.stereotype.Service;

@Service
public class GreetingServicePropertyInjected implements GreetingService {

    @Override
    public String sayGreeting() {
        return "Friends don't let friends to property injection!";
    }
}
