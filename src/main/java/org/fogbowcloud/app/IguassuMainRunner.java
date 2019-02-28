package org.fogbowcloud.app;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.IguassuController;
import org.fogbowcloud.app.core.constants.IguassuGeneralConstants;
import org.fogbowcloud.app.core.constants.IguassuPropertiesConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;

import java.io.FileInputStream;
import java.util.Properties;

public class IguassuMainRunner implements CommandLineRunner, ApplicationListener<EmbeddedServletContainerInitializedEvent> {

    private static final Logger LOGGER = Logger.getLogger(IguassuMainRunner.class);

    @Autowired
    private Properties properties;

    @Lazy
    @Autowired
    IguassuController iguassuController;

    private String iguassuConfPath = IguassuGeneralConstants.DEFAULT_IGUASSU_CONF_FILE_PATH;
    private String schedConfPath = IguassuGeneralConstants.DEFAULT_SCHED_CONF_FILE_PATH;
    private int port;

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("Running IguassuMainRunner.");
        if (args.length > 0) {
            iguassuConfPath = args[0];
            schedConfPath = args[1];
            LOGGER.info("Loaded conf paths.");
        } else {
            LOGGER.info("Loaded default conf paths.");
        }
        loadProperties(iguassuConfPath, schedConfPath, port);
    }

    @Override
    public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
        port = event.getEmbeddedServletContainer().getPort();
        LOGGER.info("Get server port.");
    }

    private void loadProperties(String iguassuConfPath, String schedConfPath, int port) {
        try {
            properties.load(new FileInputStream(iguassuConfPath));
            properties.load(new FileInputStream(schedConfPath));
            properties.setProperty(IguassuPropertiesConstants.REST_SERVER_PORT, String.valueOf(port));
            LOGGER.info("Loaded properties.");
        } catch (Exception e) {
            System.exit(1);
        }
    }
}
