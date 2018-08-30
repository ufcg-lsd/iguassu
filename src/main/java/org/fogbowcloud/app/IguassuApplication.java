package org.fogbowcloud.app;

import org.fogbowcloud.app.utils.IguassuPropertiesConstants;
import org.fogbowcloud.app.utils.IguassuGeneralConstants;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.exception.IguassuException;
import org.fogbowcloud.blowout.core.exception.BlowoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

import java.io.FileInputStream;
import java.util.Properties;

@SpringBootApplication
public class IguassuApplication {

    private final Logger LOGGER = Logger.getLogger(IguassuApplication.class);

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

    public class IguassuMainRunner implements CommandLineRunner, ApplicationListener<EmbeddedServletContainerInitializedEvent> {

        @Autowired
        private Properties properties;

        @Lazy
        @Autowired
        IguassuController iguassuController;

        @Override
        public void run(String...args) {

            String iguassuConfPath;
            String schedConfPath;

            if (args.length > 0) {
                iguassuConfPath = args[0];
                schedConfPath = args[1];
            } else {
                iguassuConfPath = IguassuGeneralConstants.DEFAULT_IGUASSU_CONF_FILE_PATH;
                schedConfPath = IguassuGeneralConstants.DEFAULT_SCHED_CONF_FILE_PATH;
            }

            try {
                properties.load(new FileInputStream(iguassuConfPath));
                properties.load(new FileInputStream(schedConfPath));
            } catch (Exception e) {
                System.exit(1);
            }
        }

        @Override
        public void onApplicationEvent(final EmbeddedServletContainerInitializedEvent event) {
            int port = event.getEmbeddedServletContainer().getPort();
            properties.setProperty(IguassuPropertiesConstants.REST_SERVER_PORT, String.valueOf(port));
        }
    }
}