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

    @Autowired
    private Properties properties;

    @Lazy
    @Autowired
    IguassuController iguassuController;

    private String iguassuConfPath = IguassuGeneralConstants.DEFAULT_IGUASSU_CONF_FILE_PATH;

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("Running IguassuMainRunner.");
        if (args.length > 0) {
            iguassuConfPath = args[0];
            LOGGER.info("Configurations imported of path " + iguassuConfPath);

        } else {
            LOGGER.info("Configurations of default path " + iguassuConfPath);
        }
        loadProperties(iguassuConfPath);
    }

    private void loadProperties(String iguassuConfPath) {
        try {
            properties.load(new FileInputStream(iguassuConfPath));
            LOGGER.info("Configurations of path " + iguassuConfPath + " loaded with success");
        } catch (Exception e) {
            System.exit(1);
        }
    }
}
