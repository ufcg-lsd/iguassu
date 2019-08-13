package org.fogbowcloud.app;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.annotation.Bean;

import java.util.Properties;

@SpringBootApplication(exclude = RepositoryRestMvcAutoConfiguration.class)
public class IguassuApplication {

    private static final String APPLICATION_PID_FILE = "./bin/shutdown.pid";

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(IguassuApplication.class);
        springApplication.addListeners(new ApplicationPidFileWriter(APPLICATION_PID_FILE));
        springApplication.run(args);
    }

    @Bean
    CommandLineRunner cmdRunner() {
        return new IguassuMainRunner();
    }

    @Bean
    public Properties properties() {
        return new Properties();
    }

}