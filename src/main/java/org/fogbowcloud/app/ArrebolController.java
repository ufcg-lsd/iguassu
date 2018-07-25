package org.fogbowcloud.app;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.datastore.JobDataStore;
import org.fogbowcloud.app.datastore.OAuthTokenDataStore;
import org.fogbowcloud.app.exception.ArrebolException;
import org.fogbowcloud.app.exception.InvalidParameterException;
import org.fogbowcloud.app.external.oauth.OAuthController;
import org.fogbowcloud.app.jdfcompiler.job.JobSpecification;
import org.fogbowcloud.app.jdfcompiler.main.CommonCompiler;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.app.jdfcompiler.main.CommonCompiler.FileType;
import org.fogbowcloud.app.model.JDFJob;
import org.fogbowcloud.app.model.JDFJobBuilder;
import org.fogbowcloud.app.model.OAuthToken;
import org.fogbowcloud.app.model.User;
import org.fogbowcloud.app.utils.ArrebolPropertiesConstants;
import org.fogbowcloud.app.utils.authenticator.ArrebolAuthenticator;
import org.fogbowcloud.app.utils.authenticator.Credential;
import org.fogbowcloud.blowout.core.BlowoutController;
import org.fogbowcloud.blowout.core.exception.BlowoutException;
import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.core.model.TaskState;
import org.fogbowcloud.blowout.core.util.AppPropertiesConstants;
import org.fogbowcloud.blowout.core.util.ManagerTimer;
import org.fogbowcloud.blowout.infrastructure.provider.fogbow.FogbowRequirementsHelper;
import org.json.JSONException;
import org.json.JSONObject;

public class ArrebolController {

	private class AsyncJobBuilder implements Runnable {

		private JDFJob job;
		private String jdfFilePath;
		private Properties properties;
		private BlowoutController blowoutController;
		private JobDataStore db;
		private JobSpecification jobSpec;
		private JDFJobBuilder jdfJobBuilder;
		private String userName;
		private String externalOAuthToken;

		AsyncJobBuilder(JDFJob job,
						String jdfFilePath,
						Properties properties,
						BlowoutController blowoutController,
						JobDataStore db,
						JobSpecification jobSpec,
						String userName,
						String externalOAuthToken) {
			this.job = job;
			this.jdfFilePath = jdfFilePath;
			this.properties = properties;
			this.blowoutController = blowoutController;
			this.db = db;
			this.jobSpec = jobSpec;
			this.jdfJobBuilder = new JDFJobBuilder(this.properties);
			this.userName = userName;
			this.externalOAuthToken = externalOAuthToken;
		}

		@Override
		public void run() {
			try {

				this.jdfJobBuilder.createJobFromJDFFile(this.job, this.jdfFilePath, this.jobSpec, this.userName, this.externalOAuthToken);
				this.blowoutController.addTaskList(job.getTasks());
				LOGGER.debug("Submitted " +job.getId()+ " to blowout at time: "+ System.currentTimeMillis());
				this.job.finishCreation();
			} catch (Exception e) {
				LOGGER.debug("Failed to Submitt " +job.getId()+ " to blowout at time: "+ System.currentTimeMillis(), e);
				this.job.failCreation();
			}
			this.db.update(job);
		}
	}

	private static final Logger LOGGER = Logger.getLogger(ArrebolController.class);

	private BlowoutController blowoutController;
	private Properties properties;
	private List<Integer> nonces;
	private HashMap<String, Task> finishedTasks;
	private HashMap<String, Thread> creatingJobs;
	private JobDataStore jobDataStore;
	private OAuthTokenDataStore oAuthTokenDataStore;
	private IguassuAuthenticator auth;
	private OAuthController externalOAuthTokenController;

    private static ManagerTimer executionMonitorTimer = new ManagerTimer(Executors.newScheduledThreadPool(1));

	public ArrebolController(Properties properties)
			throws BlowoutException, ArrebolException {
		if (properties == null) {
			throw new IllegalArgumentException("Properties cannot be null.");
		} else if (!checkProperties(properties)) {
			throw new ArrebolException("Error while initializing Arrebol Controller.");
		}
		this.finishedTasks = new HashMap<>();
		this.properties = properties;
		this.blowoutController = new BlowoutController(properties);
		this.creatingJobs = new HashMap<>();
		this.externalOAuthTokenController = new OAuthController(properties);
	}

	public Properties getProperties() {
		return properties;
	}

	public void init() throws Exception {
		// FIXME: add as constructor param?
		this.auth = new ThirdAppAuthenticator();
		// FIXME: replace by a proper
		this.jobDataStore = new JobDataStore(properties.getProperty(AppPropertiesConstants.DB_DATASTORE_URL));
		this.oAuthTokenDataStore = new OAuthTokenDataStore(this.properties.getProperty(AppPropertiesConstants.DB_DATASTORE_URL));

		Boolean removePreviousResources = Boolean.valueOf(
				this.properties.getProperty(ArrebolPropertiesConstants.REMOVE_PREVIOUS_RESOURCES)
		);

		LOGGER.debug("Properties: " + properties.getProperty(ArrebolPropertiesConstants.DEFAULT_SPECS_FILE_PATH));

		blowoutController.start(removePreviousResources);

		LOGGER.debug("Application to be started on port: " + properties.getProperty(ArrebolPropertiesConstants.REST_SERVER_PORT));
		LOGGER.info("Properties: " + properties.getProperty(AppPropertiesConstants.INFRA_INITIAL_SPECS_FILE_PATH));

		this.nonces = new ArrayList<>();

		LOGGER.debug("Restarting jobs");
		restartAllJobs();

		int schedulerPeriod = Integer.valueOf(properties.getProperty(ArrebolPropertiesConstants.EXECUTION_MONITOR_PERIOD));
		LOGGER.debug("Starting Execution Monitor, with period: " + schedulerPeriod);
        ExecutionMonitorWithDB executionMonitor = new ExecutionMonitorWithDB(
				this,
				jobDataStore
		);
		executionMonitorTimer.scheduleAtFixedRate(executionMonitor, 0, schedulerPeriod);
	}

	public void stop() {
		for (Thread t : creatingJobs.values()) {
		    while (t.isAlive()) {
                t.interrupt();
            }
		}
	}

	void restartAllJobs() throws BlowoutException {
		for (JDFJob job : this.jobDataStore.getAll()) {
			if (job.getState().equals(JDFJob.JDFJobState.SUBMITTED)) {
				job.failCreation();
				this.jobDataStore.update(job);
			}
			ArrayList<Task> taskList = new ArrayList<>();
			for (Task task : job.getTasks()) {
				if (!task.isFinished()) {
					taskList.add(task);
					LOGGER.debug("Specification of Recovered task: " + task.getSpecification().toJSON().toString());
					LOGGER.debug("Task Requirements: " + task.getSpecification()
							.getRequirementValue(FogbowRequirementsHelper.METADATA_FOGBOW_REQUIREMENTS));
				} else {
					finishedTasks.put(task.getId(), task);
				}
			}
			blowoutController.addTaskList(taskList);
		}
	}

//	private IguassuAuthenticator createAuthenticatorPluginInstance() throws Exception {
//		String providerClassName = this.properties.getProperty(ArrebolPropertiesConstants.AUTHENTICATION_PLUGIN);
//		Class<?> forName = Class.forName(providerClassName);
//		Object clazz = forName.getConstructor(Properties.class).newInstance(this.properties);
//		if (!(clazz instanceof IguassuAuthenticator)) {
//			throw new Exception("Authenticator Class Name is not a ArrebolAuthenticator implementation");
//		}
//
//		return (IguassuAuthenticator) clazz;
//	}

	public JDFJob getJobById(String jobId, String owner) {
        return this.jobDataStore.getByJobId(jobId, owner);
	}

	public String addJob(String jdfFilePath, User owner)
			throws CompilerException, NameAlreadyInUseException, BlowoutException, IOException {
		LOGGER.debug("Adding job  of owner " + owner.getUsername() + " to scheduler" );
		JDFJob job = createJobFromJDFFile(jdfFilePath, owner);
		if (job.getName() != null &&
				!job.getName().trim().isEmpty() &&
				getJobByName(job.getName(), owner.getUser()) != null) {
			throw new NameAlreadyInUseException(
					"The job name '" + job.getName() + "' is already in use for the user '" + owner.getUser() + "'."
			);
		}

		jobDataStore.insert(job);
		return job.getId();
	}

	void waitForJobCreation(String jobId) throws InterruptedException {
		creatingJobs.get(jobId).join();
	}

	JDFJob createJobFromJDFFile(String jdfFilePath, User owner) throws CompilerException, IOException {
		JDFJob job = new JDFJob(owner.getUser(), new ArrayList<Task>(), owner.getUsername());
		CommonCompiler commonCompiler = new CommonCompiler();
		LOGGER.debug("Job "+ job.getId() + " compilation started at time: "+ System.currentTimeMillis() );
		commonCompiler.compile(jdfFilePath, FileType.JDF);
		LOGGER.debug("Job "+ job.getId() + " compilation ended at time: "+ System.currentTimeMillis() );
		JobSpecification jobSpec = (JobSpecification) commonCompiler.getResult().get(0);
		String userName = owner.getUsername();
		String externalOAuthToken = getAccessTokenByOwnerUsername(userName);

		Thread t = new Thread(new AsyncJobBuilder(job, jdfFilePath, properties, blowoutController, jobDataStore, jobSpec, userName, externalOAuthToken));
		t.start();
		creatingJobs.put(job.getId(), t);
		return job;
	}

	public ArrayList<JDFJob> getAllJobs(String owner) {
		return (ArrayList<JDFJob>) this.jobDataStore.getAllByOwner(owner);
	}

	public void updateJob(JDFJob job) {
		this.jobDataStore.update(job);
	}

	public String stopJob(String jobReference, String owner) {
		JDFJob jobToRemove = getJobByName(jobReference, owner);
		if (jobToRemove == null) {
			jobToRemove = getJobById(jobReference, owner);
		}
		if (jobToRemove != null) {
			LOGGER.debug("Removing job " + jobToRemove.getName() + ".");
			Thread creatingThread = creatingJobs.get(jobToRemove.getId());
			if (creatingThread != null) {
				if (creatingThread.isAlive()) {
					LOGGER.info("Job was still being created.");
					while (creatingThread.isAlive()) {
						creatingThread.interrupt();
					}
				}
				creatingJobs.remove(jobToRemove.getId());
			}
			this.jobDataStore.deleteByJobId(jobToRemove.getId(), owner);
			for (Task task : jobToRemove.getTasks()) {
				LOGGER.debug("Removing task " + task.getId() + " from job.");
				blowoutController.cleanTask(task);
			}
			return jobToRemove.getId();
		}
		return null;
	}

	public JDFJob getJobByName(String jobName, String owner) {
		if (jobName == null) {
			return null;
		}
		for (JDFJob job : this.jobDataStore.getAllByOwner(owner)) {
			if (jobName.equals(job.getName())) {
				return job;
			}
		}
		return null;
	}

	public Task getTaskById(String taskId, String owner) {
		for (JDFJob job : getAllJobs(owner)) {
            Task task = job.getTaskById(taskId);
			if (task != null) {
				return task;
			}
		}
		return null;
	}

	public TaskState getTaskState(String taskId) {
		Task task = finishedTasks.get(taskId);
		if (task != null) {
			return TaskState.COMPLETED;
		} else {
			return blowoutController.getTaskState(taskId);
		}
	}

	public void moveTaskToFinished(Task task) {
		JDFJob job = this.jobDataStore.getByJobId(
				task.getMetadata(ArrebolPropertiesConstants.JOB_ID),
				task.getMetadata(ArrebolPropertiesConstants.OWNER)
		);
		LOGGER.debug("Moving task " + task.getId() + "from job " + job.getName() +" to finished");
		finishedTasks.put(task.getId(), task);
		job.finish(task);
		updateJob(job);
		blowoutController.cleanTask(task);
	}

	public User authUser(String credentials) throws IOException, GeneralSecurityException {
		if (credentials == null) {
			return null;
		}

		Credential credential;
		try {
			JSONObject jsonObject = new JSONObject(credentials);
			credential = Credential.fromJSON(jsonObject);
		} catch (JSONException e) {
			LOGGER.error("Invalid credentials format", e);
			return null;
		}

		User user = null;
		LOGGER.debug("Checking nonce");
		if (this.nonces.contains(credential.getNonce())) {
			nonces.remove(credential.getNonce());
			user = this.auth.authenticateUser(credential);
		}
		return user;
	}

	public int getNonce() {
		int nonce = UUID.randomUUID().hashCode();
		this.nonces.add(nonce);
		return nonce;
	}

	public User getUser(String username) {
		return this.auth.getUserByUsername(username);
	}

	public User addUser(String username, String publicKey) {
		try {
			return this.auth.addUser(username, publicKey);
		} catch (Exception e) {
			throw new RuntimeException("Could not add user", e);
		}
	}

	public String getAuthenticatorName() {
		return this.auth.getAuthenticatorName();
	}

	public void setBlowoutController(BlowoutController blowout) {
		this.blowoutController = blowout;
	}

	public JobDataStore getJobDataStore() {
		return this.jobDataStore;
	}

	public void setDataStore(JobDataStore dataStore) {
		this.jobDataStore = dataStore;
	}

	private static String requiredPropertyMessage(String property) {
		return "Required property " + property + " was not set";
	}

	private static boolean checkProperties(Properties properties) {
		// Arrebol required properties
		if (!properties.containsKey(ArrebolPropertiesConstants.EXECUTION_MONITOR_PERIOD)) {
			LOGGER.error(requiredPropertyMessage(ArrebolPropertiesConstants.EXECUTION_MONITOR_PERIOD));
			return false;
		}
		if (!properties.containsKey(ArrebolPropertiesConstants.PUBLIC_KEY_CONSTANT)) {
			LOGGER.error(requiredPropertyMessage(ArrebolPropertiesConstants.PUBLIC_KEY_CONSTANT));
			return false;
		}
		if (!properties.containsKey(ArrebolPropertiesConstants.PRIVATE_KEY_FILEPATH)) {
			LOGGER.error(requiredPropertyMessage(ArrebolPropertiesConstants.PRIVATE_KEY_FILEPATH));
			return false;
		}
		if (!properties.containsKey(ArrebolPropertiesConstants.REMOTE_OUTPUT_FOLDER)) {
			LOGGER.error(requiredPropertyMessage(ArrebolPropertiesConstants.REMOTE_OUTPUT_FOLDER));
			return false;
		}
		if (!properties.containsKey(ArrebolPropertiesConstants.LOCAL_OUTPUT_FOLDER)) {
			LOGGER.error(requiredPropertyMessage(ArrebolPropertiesConstants.LOCAL_OUTPUT_FOLDER));
			return false;
		}
		if (properties.containsKey(ArrebolPropertiesConstants.ENCRYPTION_TYPE)) {
			try {
				MessageDigest.getInstance(properties.getProperty(ArrebolPropertiesConstants.ENCRYPTION_TYPE));
			} catch (NoSuchAlgorithmException e) {
				String builder = "Property " +
						ArrebolPropertiesConstants.ENCRYPTION_TYPE +
						"(" +
						properties.getProperty(ArrebolPropertiesConstants.ENCRYPTION_TYPE) +
						") does not refer to a valid encryption algorithm." +
						" Valid options are 'MD5', 'SHA-1' and 'SHA-256'.";
				LOGGER.error(builder);
				return false;
			}
		}
//		if (!properties.containsKey(ArrebolPropertiesConstants.AUTHENTICATION_PLUGIN)) {
//			LOGGER.error(requiredPropertyMessage(ArrebolPropertiesConstants.AUTHENTICATION_PLUGIN));
//			return false;
//		} else {
//			String authenticationPlugin = properties.getProperty(ArrebolPropertiesConstants.AUTHENTICATION_PLUGIN);
//			if (authenticationPlugin.equals("org.fogbowcloud.app.utils.LDAPAuthenticator")) {
//				if (!properties.containsKey(ArrebolPropertiesConstants.LDAP_AUTHENTICATION_URL)) {
//					LOGGER.error(requiredPropertyMessage(ArrebolPropertiesConstants.LDAP_AUTHENTICATION_URL));
//					return false;
//				}
//				if (!properties.containsKey(ArrebolPropertiesConstants.LDAP_AUTHENTICATION_BASE)) {
//					LOGGER.error(requiredPropertyMessage(ArrebolPropertiesConstants.LDAP_AUTHENTICATION_BASE));
//					return false;
//				}
//			}
//		}
		LOGGER.debug("All properties are set");
		return true;
	}

	public int getTaskRetries(String taskId, String owner) {
		Task task = finishedTasks.get(taskId);
		if (task != null) {
			return task.getRetries();
		} else {
			task = getTaskById(taskId, owner);
			if (task != null) {
				return blowoutController.getTaskRetries(task.getId());
				
			}
			return 0;
		}
	}

	public boolean storeOAuthToken(OAuthToken oAuthToken) {
		boolean saved = this.oAuthTokenDataStore.insert(oAuthToken);
		return saved;
	}

	public List<OAuthToken> getAllOAuthTokens() {
		return this.oAuthTokenDataStore.getAll();
	}

	public String getAccessTokenByOwnerUsername(String ownerUsername) {
		List<OAuthToken> tokensList = this.oAuthTokenDataStore.getAccessTokenByOwnerUsername(ownerUsername);

		String accessToken = null;
		for (OAuthToken token: tokensList) {
			if (!token.hasExpired()) {
				accessToken =  token.getAccessToken();
			}
		}

		if (accessToken == null && tokensList.size() != 0) {
			accessToken = refreshExternalOAuthToken(ownerUsername);
		}

		return accessToken;
	}

	public void deleteOAuthTokenByAcessToken(String accessToken) {
		this.oAuthTokenDataStore.deleteByAccessToken(accessToken);
	}

	public void deleteAllExternalOAuthTokens() {
		this.oAuthTokenDataStore.deleteAll();
	}

	public String refreshExternalOAuthToken(String ownerUsername) {
		List<OAuthToken> tokensList = this.oAuthTokenDataStore.getAccessTokenByOwnerUsername(ownerUsername);
		if (tokensList.size() == 0) {

		}
		OAuthToken someToken = tokensList.get(0);
		String someRefreshToken = someToken.getRefreshToken();
		OAuthToken newOAuthToken = this.externalOAuthTokenController.refreshToken(someRefreshToken);
		String accessToken = newOAuthToken.getAccessToken();
		deleteTokens(tokensList);
		storeOAuthToken(newOAuthToken);
		return accessToken;
	}

	private void deleteTokens(List<OAuthToken> tokenList) {
		for (OAuthToken token: tokenList) {
			deleteOAuthTokenByAcessToken(token.getAccessToken());
		}
	}

}
