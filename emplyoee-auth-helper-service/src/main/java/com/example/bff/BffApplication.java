package com.example.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BffApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BffApplication.class);
        // If no server.port is provided via env/command-line/properties, default to 8082
        boolean hasPort = System.getProperty("server.port") != null || System.getenv("SERVER_PORT") != null;
        if (!hasPort) {
            app.setDefaultProperties(java.util.Collections.singletonMap("server.port", "8081"));
        }
        app.run(args);
    }
}
