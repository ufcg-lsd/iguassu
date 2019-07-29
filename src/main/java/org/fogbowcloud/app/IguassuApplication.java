package org.fogbowcloud.app;

import java.util.Properties;

import org.fogbowcloud.app.core.IguassuController;
import org.fogbowcloud.app.core.IguassuFacade;
import org.fogbowcloud.app.core.exceptions.IguassuException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.system.ApplicationPidFileWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

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

    @Bean
    @Lazy
    public IguassuFacade iguassuFacade(Properties properties) {
        final IguassuController iguassuController = new IguassuController(properties);
        final IguassuFacade iguassuFacade = new IguassuFacade(iguassuController);

        try {
            iguassuFacade.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return iguassuFacade;
    }
}