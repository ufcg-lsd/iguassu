package org.fogbowcloud.app.restlet;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.ArrebolController;
import org.fogbowcloud.app.NameAlreadyInUseException;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.app.model.JDFJob;
import org.fogbowcloud.app.model.User;
import org.fogbowcloud.app.resource.AuthenticationResource;
import org.fogbowcloud.app.resource.JobResource;
import org.fogbowcloud.app.resource.NonceResource;
import org.fogbowcloud.app.resource.UserResource;
import org.fogbowcloud.app.utils.ArrebolPropertiesConstants;
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

	private ArrebolController arrebolController;
	private Component restletComponent;
	private static final Logger LOGGER = Logger
			.getLogger(JDFSchedulerApplication.class);

	public JDFSchedulerApplication(ArrebolController arrebolController)
			throws Exception {
		this.arrebolController = arrebolController;
		this.arrebolController.init();
	}

	public void startServer() throws Exception {
		Properties properties = this.arrebolController.getProperties();
		if (!properties.containsKey(ArrebolPropertiesConstants.REST_SERVER_PORT)) {
			throw new IllegalArgumentException(
					ArrebolPropertiesConstants.REST_SERVER_PORT
							+ " is missing on properties.");
		}
		Integer restServerPort = Integer.valueOf((String) properties
				.get(ArrebolPropertiesConstants.REST_SERVER_PORT));

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
		this.arrebolController.stop();
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
		return this.arrebolController.getJobById(jobId, owner);
	}

	public String addJob(String jdfFilePath, User owner)
			throws CompilerException, NameAlreadyInUseException, BlowoutException, IOException {
		return this.arrebolController.addJob(jdfFilePath, owner);
	}

	public ArrayList<JDFJob> getAllJobs(String owner) {
		return this.arrebolController.getAllJobs(owner);
	}

	public String stopJob(String jobId, String owner) {
		return this.arrebolController.stopJob(jobId, owner);
	}

	public JDFJob getJobByName(String jobName, String owner) {
		return this.arrebolController.getJobByName(jobName, owner);
	}

	public Task getTaskById(String taskId, String owner) {
		return this.arrebolController.getTaskById(taskId, owner);
	}

	public TaskState getTaskState(String taskId) {
		return this.arrebolController.getTaskState(taskId);
	}

	public int getNonce() {
		return this.arrebolController.getNonce();
	}

	public User authUser(String credentials)
			throws IOException, GeneralSecurityException {
		return this.arrebolController.authUser(credentials);
	}

	public User getUser(String username) {
		return this.arrebolController.getUser(username);
	}

	public User addUser(String username, String publicKey) {
		return this.arrebolController.addUser(username, publicKey);
	}

	public String getAuthenticatorName() {
		
		return this.arrebolController.getAuthenticatorName();
	}

	public int getTaskRetries(String taskId, String owner) {
		return this.arrebolController.getTaskRetries(taskId, owner);
	}
}
