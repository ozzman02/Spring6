package com.ossant.di.controllers;

import com.ossant.di.services.GreetingService;
import com.ossant.di.services.GreetingServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

@Controller
public class MyController {

    Logger logger = LoggerFactory.getLogger(MyController.class);

    private final GreetingService greetingService;

    public MyController() {
        this.greetingService = new GreetingServiceImpl();
    }


    public String sayHello() {
        logger.info("I'm in the controller");
        return greetingService.sayGreeting();
    }

    public void beforeInit(){
        System.out.println("## - Before Init - Called by Bean Post Processor");
    }

    public void afterInit(){
        System.out.println("## - After init called by Bean Post Processor");
    }

}
