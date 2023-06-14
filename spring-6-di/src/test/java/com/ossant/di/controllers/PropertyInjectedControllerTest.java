package com.ossant.di.controllers;

import com.ossant.di.services.GreetingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"EN", "default"})
@SpringBootTest
class PropertyInjectedControllerTest {

    Logger logger = LoggerFactory.getLogger(PropertyInjectedControllerTest.class);

    @Autowired
    PropertyInjectedController propertyInjectedController;

    /*@BeforeEach
    void setUp() {
        propertyInjectedController = new PropertyInjectedController();
        propertyInjectedController.greetingService = new GreetingServiceImpl();
    }*/

    @Test
    void sayHello() {
        logger.info(propertyInjectedController.sayHello());

    }

}