package org.devconnect.devconnectbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class DevConnectBackendApplication {

    public static void main(String[] args) {

        // IoC Container is of type ApplicationContext
        ApplicationContext container = SpringApplication.run(DevConnectBackendApplication.class, args);
    }

}
