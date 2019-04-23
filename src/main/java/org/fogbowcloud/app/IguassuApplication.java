package org.fogbowcloud.app;

import org.fogbowcloud.app.core.IguassuController;
import org.fogbowcloud.app.core.IguassuFacade;
import org.fogbowcloud.app.core.exceptions.IguassuException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
        SpringApplication.run(IguassuApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        final String[] HTTP_METHODS_SUPPORTED = { "GET", "POST", "PUT", "DELETE", "OPTIONS" };
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*").
                        allowedHeaders("*")
                        .allowedMethods(HTTP_METHODS_SUPPORTED);
            }
        };
    }
}