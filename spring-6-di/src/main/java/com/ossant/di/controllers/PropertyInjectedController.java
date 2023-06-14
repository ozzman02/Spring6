package com.ossant.di.controllers;

import com.ossant.di.services.GreetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;

@Controller
public class PropertyInjectedController {

    @Qualifier("greetingServicePropertyInjected")
    @Autowired
    GreetingService greetingService;

    public String sayHello() {
        return greetingService.sayGreeting();
    }

}
