package com.juanroy.mybooklist;

import com.juanroy.mybooklist.config.DatabaseUrlInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyBookListApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(MyBookListApplication.class);
        application.addInitializers(new DatabaseUrlInitializer());
        application.run(args);
    }

}
