package com.ossant.di.controllers;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"EN", "default"})
@SpringBootTest
class SetterInjectedControllerTest {

    Logger logger = LoggerFactory.getLogger(SetterInjectedControllerTest.class);

    @Autowired
    SetterInjectedController setterInjectedController;

    @Test
    void sayHello() {
        logger.info(setterInjectedController.sayHello());
    }
}