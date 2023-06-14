package com.ossant.di;

import com.ossant.di.controllers.MyController;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles({"EN", "default"})
@SpringBootTest
class Spring6DiApplicationTests {

	static Logger logger = LoggerFactory.getLogger(Spring6DiApplicationTests.class);

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	MyController controller;

	@Test
	void testAutowiredOfController() {
		logger.info(controller.sayHello());
	}

	@Test
	void testGetControllerFromContext() {
		MyController controller = applicationContext.getBean(MyController.class);
		logger.info(controller.sayHello());
	}

	@Test
	void contextLoads() {
	}

}
