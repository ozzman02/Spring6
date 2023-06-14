package com.ossant.di.controllers;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

class MyControllerTest {

    Logger logger = LoggerFactory.getLogger(MyControllerTest.class);

    @Test
    void sayHello() {
        MyController myController = new MyController();
        logger.info(myController.sayHello());
    }
}