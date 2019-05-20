package org.fogbowcloud.app;

import org.fogbowcloud.app.core.IguassuController;
import org.fogbowcloud.app.core.IguassuFacade;
import org.fogbowcloud.app.core.exceptions.IguassuException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationPidFileWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

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

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(IguassuApplication.class);
        springApplication.addListeners(new ApplicationPidFileWriter("./bin/shutdown.pid"));
        springApplication.run(args);
    }
}