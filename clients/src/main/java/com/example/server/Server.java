package com.example.server;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Our Spring Boot application.
 */
@SpringBootApplication
@EnableSwagger2
public class Server {
    /**
     * Starts our Spring Boot application.
     */
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Server.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.setWebApplicationType(WebApplicationType.SERVLET);
        app.run(args);
    }
}
