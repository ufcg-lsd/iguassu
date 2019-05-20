package org.fogbowcloud.app;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.IguassuController;
import org.fogbowcloud.app.core.constants.IguassuGeneralConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;

import java.io.FileInputStream;
import java.util.Properties;

public class IguassuMainRunner implements CommandLineRunner {

    private static final Logger LOGGER = Logger.getLogger(IguassuMainRunner.class);
    private static String CONF_FILE_PATH = IguassuGeneralConstants.DEFAULT_IGUASSU_CONF_FILE_PATH;

    @Autowired
    private Properties properties;

    @Lazy
    @Autowired
    IguassuController iguassuController;

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("Running " + IguassuMainRunner.class.getName());
        if (args.length > 0) {
            CONF_FILE_PATH = args[0];
            LOGGER.info("Configurations imported of path " + CONF_FILE_PATH + ".");

        } else {
            LOGGER.info("Configurations of default path " + CONF_FILE_PATH + ".");
        }
        loadProperties(CONF_FILE_PATH);
    }

    private void loadProperties(String iguassuConfPath) {
        try {
            properties.load(new FileInputStream(iguassuConfPath));
            LOGGER.info("Configurations of file " + iguassuConfPath + " was loaded with success.");
        } catch (Exception e) {
            LOGGER.info("Configuration file was not founded or not loaded with success.");
            System.exit(1);
        }
    }
}
