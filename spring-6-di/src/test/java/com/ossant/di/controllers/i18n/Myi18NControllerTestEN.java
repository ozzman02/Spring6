package com.ossant.di.controllers.i18n;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"EN", "default"})
@SpringBootTest
class Myi18NControllerTestEN {

    Logger logger = LoggerFactory.getLogger(Myi18NControllerTestEN.class);

    @Autowired
    Myi18NController myi18NController;

    @Test
    void sayHello() {
        logger.info(myi18NController.sayHello());
    }
}