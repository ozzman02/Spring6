package com.ossant.di;

import com.ossant.di.controllers.MyController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Spring6DiApplication {

	static Logger logger = LoggerFactory.getLogger(Spring6DiApplication.class);

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(Spring6DiApplication.class, args);
		MyController controller = ctx.getBean(MyController.class);
		logger.info("In Main Method");
		logger.info(controller.sayHello());
	}

}
