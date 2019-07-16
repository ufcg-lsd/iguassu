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

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(IguassuApplication.class);
        springApplication.addListeners(new ApplicationPidFileWriter("./bin/shutdown.pid"));
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
    public IguassuFacade iguassuFacade(Properties properties) throws IguassuException {
        IguassuController iguassuController = new IguassuController(properties);
        IguassuFacade iguassuFacade = new IguassuFacade(iguassuController);

        try {
            iguassuFacade.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return iguassuFacade;
    }
}