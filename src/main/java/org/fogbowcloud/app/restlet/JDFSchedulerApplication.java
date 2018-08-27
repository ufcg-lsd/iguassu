package org.fogbowcloud.app.restlet;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.IguassuController;
import org.fogbowcloud.app.NameAlreadyInUseException;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.app.model.JDFJob;
import org.fogbowcloud.app.model.User;
import org.fogbowcloud.app.resource.AuthenticationResource;
import org.fogbowcloud.app.resource.JobResource;
import org.fogbowcloud.app.resource.NonceResource;
import org.fogbowcloud.app.resource.UserResource;
import org.fogbowcloud.app.utils.IguassuPropertiesConstants;
import org.fogbowcloud.blowout.core.exception.BlowoutException;
import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.core.model.TaskState;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;
import org.restlet.service.ConnectorService;

public class JDFSchedulerApplication extends Application {

	private static final String ARREBOL_JOB_PATH = "/arrebol/job";
	private static final String ARREBOL_JOB_ID_PATH = "/arrebol/job/{jobpath}";
	private static final String ARREBOL_NONCE_PATH = "/arrebol/nonce";
	private static final String ARREBOL_AUTHENTICATOR_PATH = "/arrebol/authenticator";
	private static final String ARREBOL_USER_PATH = "/arrebol/user";

	private IguassuController iguassuController;
	private Component restletComponent;
	private static final Logger LOGGER = Logger
			.getLogger(JDFSchedulerApplication.class);

	public JDFSchedulerApplication(IguassuController iguassuController)
			throws Exception {
		this.iguassuController = iguassuController;
		this.iguassuController.init();
	}

	public void startServer() throws Exception {
		Properties properties = this.iguassuController.getProperties();
		if (!properties.containsKey(IguassuPropertiesConstants.REST_SERVER_PORT)) {
			throw new IllegalArgumentException(
					IguassuPropertiesConstants.REST_SERVER_PORT
							+ " is missing on properties.");
		}
		Integer restServerPort = Integer.valueOf((String) properties
				.get(IguassuPropertiesConstants.REST_SERVER_PORT));

		LOGGER.info("Starting service on port: " + restServerPort);

		ConnectorService corsService = new ConnectorService();
		this.getServices().add(corsService);

		this.restletComponent = new Component();
		this.restletComponent.getServers().add(Protocol.HTTP, restServerPort);
		this.restletComponent.getDefaultHost().attach(this);
		this.restletComponent.start();
	}

	public void stopServer() throws Exception {
		this.restletComponent.stop();
		this.iguassuController.stop();
	}

	@Override
	public Restlet createInboundRoot() {
		Router router = new Router(getContext());
		router.attach(ARREBOL_JOB_PATH, JobResource.class);
		router.attach(ARREBOL_JOB_ID_PATH, JobResource.class);
		router.attach(ARREBOL_NONCE_PATH, NonceResource.class);
		router.attach(ARREBOL_AUTHENTICATOR_PATH, AuthenticationResource.class);
		router.attach(ARREBOL_USER_PATH, UserResource.class);
		return router;
	}

	public JDFJob getJobById(String jobId, String owner) {
		return this.iguassuController.getJobById(jobId, owner);
	}

	public String addJob(String jdfFilePath, User owner)
			throws CompilerException, NameAlreadyInUseException, BlowoutException, IOException {
		return this.iguassuController.addJob(jdfFilePath, owner);
	}

	public ArrayList<JDFJob> getAllJobs(String owner) {
		return this.iguassuController.getAllJobs(owner);
	}

	public String stopJob(String jobId, String owner) {
		return this.iguassuController.stopJob(jobId, owner);
	}

	public JDFJob getJobByName(String jobName, String owner) {
		return this.iguassuController.getJobByName(jobName, owner);
	}

	public Task getTaskById(String taskId, String owner) {
		return this.iguassuController.getTaskById(taskId, owner);
	}

	public TaskState getTaskState(String taskId) {
		return this.iguassuController.getTaskState(taskId);
	}

	public int getNonce() {
		return this.iguassuController.getNonce();
	}

	public User authUser(String credentials)
			throws IOException, GeneralSecurityException {
		return this.iguassuController.authUser(credentials);
	}

	public User getUser(String username) {
		return this.iguassuController.getUser(username);
	}

	public User addUser(String username, String publicKey) {
		return this.iguassuController.addUser(username, publicKey);
	}

	public String getAuthenticatorName() {
		
		return this.iguassuController.getAuthenticatorName();
	}

	public int getTaskRetries(String taskId, String owner) {
		return this.iguassuController.getTaskRetries(taskId, owner);
	}
}
