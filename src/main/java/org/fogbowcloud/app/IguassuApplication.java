package org.fogbowcloud.app;

import org.fogbowcloud.app.utils.ArrebolPropertiesConstants;
import org.fogbowcloud.app.utils.authenticator.IguassuGeneralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.fogbowcloud.app.exception.ArrebolException;
import org.fogbowcloud.blowout.core.exception.BlowoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.util.Properties;

// TODO: change script bin/start-arrebol-service to run this class instead of old ArrebolMain
@SpringBootApplication
public class IguassuApplication {

    private final Logger LOGGER = LoggerFactory.getLogger(IguassuApplication.class);

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
    public ArrebolController arrebolController(Properties properties) throws BlowoutException, ArrebolException {
        ArrebolController arrebolController = new ArrebolController(properties);
        try {
            arrebolController.init(); //TODO: resolve this problem with authentication
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrebolController;
    }

    public static void main(String[] args) {
        SpringApplication.run(IguassuApplication.class, args);
    }

    public class IguassuMainRunner implements CommandLineRunner, ApplicationListener<EmbeddedServletContainerInitializedEvent> {

        @Autowired
        private Properties properties;

        @Lazy
        @Autowired
        ArrebolController arrebolController;

        @Override
        public void run(String...args) {

            String arrebolConfPath;
            String schedConfPath;

            if (args.length > 0) {
                arrebolConfPath = args[0];
                schedConfPath = args[1];
            } else {
                arrebolConfPath = IguassuGeneralConstants.DEFAULT_ARREBOL_CONF_FILE_PATH;
                schedConfPath = IguassuGeneralConstants.DEFAULT_SCHED_CONF_FILE_PATH;
            }

            try {
                properties.load(new FileInputStream(arrebolConfPath));
                properties.load(new FileInputStream(schedConfPath));
            } catch (Exception e) {
                System.exit(1);
            }

        }

        @Override
        public void onApplicationEvent(final EmbeddedServletContainerInitializedEvent event) {
            int port = event.getEmbeddedServletContainer().getPort();
            properties.setProperty(ArrebolPropertiesConstants.REST_SERVER_PORT, String.valueOf(port));
        }
    }
}