package org.fogbowcloud.app;

import org.fogbowcloud.app.core.IguassuController;
import org.fogbowcloud.app.core.exceptions.IguassuException;
import org.fogbowcloud.blowout.core.exception.BlowoutException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import java.util.Properties;

@SpringBootApplication
public class IguassuApplication {
    @Bean
    CommandLineRunner cmdRunner() {
        return new IguassuMainRunner();
    }

    @Bean
    public Properties properties() {
        return new Properties();
    }

    @Bean
    @Lazy
    public IguassuController iguassuController(Properties properties) throws BlowoutException, IguassuException {
        IguassuController iguassuController = new IguassuController(properties);
        try {
            iguassuController.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return iguassuController;
    }

    public static void main(String[] args) {
        SpringApplication.run(IguassuApplication.class, args);
    }

}