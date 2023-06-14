package com.ossant.di.controllers.i18n;

import com.ossant.di.services.GreetingService;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Qualifier;

@Controller
public class Myi18NController {

    private final GreetingService greetingService;

    public Myi18NController(@Qualifier("i18NService") GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    public String sayHello(){
        return greetingService.sayGreeting();
    }
}
