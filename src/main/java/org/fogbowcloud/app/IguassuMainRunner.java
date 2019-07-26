package org.fogbowcloud.app;

import java.io.FileInputStream;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.constants.ConfProperties;
import org.fogbowcloud.app.core.constants.ExitCode;
import org.fogbowcloud.app.utils.ConfValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

public class IguassuMainRunner implements CommandLineRunner {

	private static final Logger logger = Logger.getLogger(IguassuMainRunner.class);
	private static String CONF_FILE_PATH = ConfProperties.IGUASSU_CONF_FILE;

	@Autowired private Properties properties;

	@Override
	public void run(String... args) throws Exception {
		logger.info("Running " + IguassuMainRunner.class.getName());
		if (args.length > 0) {
			CONF_FILE_PATH = args[0];
			logger.info("Configuration file found in path " + CONF_FILE_PATH + ".");

		} else {
			logger.info("Configuration file found in default path " + CONF_FILE_PATH + ".");
		}
		loadProperties(CONF_FILE_PATH);
	}

	private void loadProperties(String iguassuConfFilePath) {
		try {
			properties.load(new FileInputStream(iguassuConfFilePath));
			logger.info("Configuration file " + iguassuConfFilePath + " was loaded with success.");
			ConfValidator.validate(this.properties);
		} catch (Exception e) {
			logger.info("Configuration file was not founded or not loaded with success.");
			System.exit(ExitCode.FAIL.getCode());
		}
	}
}
