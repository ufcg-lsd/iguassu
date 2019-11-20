package org.fogbowcloud.app;

import java.io.FileInputStream;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.ApplicationFacade;
import org.fogbowcloud.app.core.constants.AppConstant;
import org.fogbowcloud.app.core.constants.ExitCode;
import org.fogbowcloud.app.core.datastore.managers.JobDBManager;
import org.fogbowcloud.app.core.datastore.managers.QueueDBManager;
import org.fogbowcloud.app.core.datastore.managers.UserDBManager;
import org.fogbowcloud.app.core.datastore.repositories.JobRepository;
import org.fogbowcloud.app.core.datastore.repositories.QueueRepository;
import org.fogbowcloud.app.core.datastore.repositories.TaskRepository;
import org.fogbowcloud.app.core.datastore.repositories.UserRepository;
import org.fogbowcloud.app.utils.ConfValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

public class IguassuMainRunner implements CommandLineRunner {

	private static final Logger logger = Logger.getLogger(IguassuMainRunner.class);
	private static String CONF_FILE_PATH = AppConstant.CONF_FILE_PATH;

	@Autowired Properties properties;

	@Autowired
	UserRepository userRepository;

	@Autowired
	JobRepository jobRepository;

	@Autowired
	QueueRepository queueRepository;

	@Autowired
	TaskRepository taskRepository;


	@Override
	public void run(String... args) throws Exception {
		JobDBManager.getInstance().setJobRepository(jobRepository);
		JobDBManager.getInstance().setTaskRepository(taskRepository);
		UserDBManager.getInstance().setUserRepository(userRepository);
		QueueDBManager.getInstance().setQueueRepository(queueRepository);
		QueueDBManager.getInstance().init();

		logger.info("Running " + IguassuMainRunner.class.getName());
		if (args.length > 0) {
			CONF_FILE_PATH = args[0];
			logger.info("Configuration file found in path " + CONF_FILE_PATH + ".");

		} else {
			logger.info("Configuration file found in default path " + CONF_FILE_PATH + ".");
		}
		loadProperties(CONF_FILE_PATH);

		try {
			ApplicationFacade.getInstance().init(this.properties);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}

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
