package org.fogbowcloud.app;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.util.Properties;


// TODO: change script bin/start-arrebol-service to run this class instead of old ArrebolMain
@SpringBootApplication
public class IguassuApplication {

    public static final Logger LOGGER = Logger.getLogger(IguassuApplication.class);

    @Bean
    CommandLineRunner cmdRunner() {
        return new IguassuMainRunner();
    }

    @Bean
    public Properties properties() {
        return new Properties();
    }

    public static void main(String[] args) {
        SpringApplication.run(IguassuApplication.class, args);
    }


    public class IguassuMainRunner implements CommandLineRunner {

        @Autowired
        private Properties properties;

        @Override
        public void run(String...args) throws Exception {

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
            /*
            try {
                final JDFSchedulerApplication app = new JDFSchedulerApplication(
                        new ArrebolController(properties)
                );
                final Thread mainThread = Thread.currentThread();
                Runtime.getRuntime().addShutdownHook(
                        new Thread() {
                            @Override
                            public void run() {
                                System.out.println("Exiting server");
                                try {
                                    app.stopServer();
                                    mainThread.join();
                                } catch (Exception e) {
                                    System.err.println(e.getMessage() + "-----");
                                    e.printStackTrace();
                                }
                            }
                        }
                );
                app.startServer();
            } catch (BlowoutException e) {
                LOGGER.error("Failed to initialize Blowout.", e);
                System.exit(1);
            } catch (Exception e) {
                LOGGER.error("Failed to initialize Arrebol.", e);
                System.exit(1);
            }
            */
        }

    }

    @Component
    public class PortListener implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {

        @Override
        public void onApplicationEvent(final EmbeddedServletContainerInitializedEvent event) {
            int thePort = event.getEmbeddedServletContainer().getPort();
            LOGGER.info("Serving on port " + String.valueOf(thePort));
            System.out.println("Serving on port " + String.valueOf(thePort));
        }
    }

}