package org.fogbowcloud.app.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.authenticator.IguassuAuthenticator;
import org.fogbowcloud.app.core.authenticator.ThirdAppAuthenticator;
import org.fogbowcloud.app.core.authenticator.models.Credential;
import org.fogbowcloud.app.core.authenticator.models.User;
import org.fogbowcloud.app.core.constants.IguassuGeneralConstants;
import org.fogbowcloud.app.core.datastore.JobDataStore;
import org.fogbowcloud.app.core.datastore.OAuthToken;
import org.fogbowcloud.app.core.datastore.OAuthTokenDataStore;
import org.fogbowcloud.app.core.exceptions.IguassuException;
import org.fogbowcloud.app.core.monitor.JobStateMonitor;
import org.fogbowcloud.app.core.task.Task;
import org.fogbowcloud.app.external.ExternalOAuthConstants;
import org.fogbowcloud.app.external.ExternalOAuthController;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.job.JDFJobBuilder;
import org.fogbowcloud.app.jdfcompiler.job.JobSpecification;
import org.fogbowcloud.app.jdfcompiler.main.CommonCompiler;
import org.fogbowcloud.app.jdfcompiler.main.CommonCompiler.FileType;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.app.jes.JobExecutionSystem;
import org.fogbowcloud.app.jes.arrebol.ArrebolJobExecutionSystem;
import org.fogbowcloud.app.jes.arrebol.ArrebolJobSynchronizer;
import org.fogbowcloud.app.jes.arrebol.JobSynchronizer;
import org.fogbowcloud.app.utils.JDFUtil;
import org.fogbowcloud.app.utils.ManagerTimer;
import org.json.JSONException;
import org.json.JSONObject;

public class IguassuController {

  private static final Logger LOGGER = Logger.getLogger(IguassuController.class);
  private static ManagerTimer executionMonitorTimer = new ManagerTimer(
      Executors.newScheduledThreadPool(1));
  private final Properties properties;
  private List<Integer> nonces;
  private JobDataStore jobDataStore;
  private OAuthTokenDataStore oAuthTokenDataStore;
  private IguassuAuthenticator authenticator;
  private ExternalOAuthController externalOAuthTokenController;
  private JobExecutionSystem jobExecutionSystem;
  private JDFJobBuilder jobBuilder;

  public IguassuController(Properties properties) throws IguassuException {
    validateProperties(properties);
    this.properties = properties;
    this.externalOAuthTokenController = new ExternalOAuthController(properties);
    this.authenticator = new ThirdAppAuthenticator();
    this.jobExecutionSystem = new ArrebolJobExecutionSystem(this.properties);
  }

  private static String requiredPropertyMessage(String property) {
    return "Required property " + property + " was not set";
  }

  private static boolean checkProperties(Properties properties) {
    if (!properties.containsKey(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_CLIENT_ID)) {
      LOGGER.error(requiredPropertyMessage(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_CLIENT_ID));
      return false;
    }

    if (!properties.containsKey(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_CLIENT_SECRET)) {
      LOGGER.error(
          requiredPropertyMessage(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_CLIENT_SECRET));
      return false;
    }

    if (!properties.containsKey(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_TOKEN_URL)) {
      LOGGER.error(requiredPropertyMessage(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_TOKEN_URL));
      return false;
    }

    LOGGER.debug("All properties are set");
    return true;
  }

  public Properties getProperties() {
    return this.properties;
  }

  public void init() {
    this.jobDataStore =
        new JobDataStore(
            this.properties.getProperty(IguassuGeneralConstants.DB_DATASTORE_URL));
    this.oAuthTokenDataStore =
        new OAuthTokenDataStore(
            this.properties.getProperty(IguassuGeneralConstants.DB_DATASTORE_URL));
    this.jobBuilder = new JDFJobBuilder(this.properties, this.oAuthTokenDataStore);
    JobSynchronizer jobSynchronizer = new ArrebolJobSynchronizer(properties);

    this.nonces = new ArrayList<>();
    JobStateMonitor jobStateMonitor = new JobStateMonitor(jobDataStore, jobSynchronizer);
    executionMonitorTimer.scheduleAtFixedRate(jobStateMonitor, 3000, 5000);
  }

  public JDFJob getJobById(String jobId, String owner) {
    return this.jobDataStore.getByJobId(jobId, owner);
  }

  public String submitJob(String jdfFilePath, User owner)
      throws CompilerException {
    LOGGER.debug("Submitting job of owner " + owner.getUserIdentification() + " to scheduler.");

    JDFJob job = buildJob(jdfFilePath, owner);

    String joIdArrebol = this.jobExecutionSystem.execute(job);
    job.setJobIdArrebol(joIdArrebol);

    LOGGER.info("Iguassu Job [" + job.getId() + "] has arrebol id: [" + job.getJobIdArrebol() + "]");

    this.jobDataStore.insert(job);

    return job.getId();
  }

  public JDFJob buildJob(String jdfFilePath, User owner) throws CompilerException {
    String userIdentification = owner.getUserIdentification();
    JDFJob job = new JDFJob(owner.getUserIdentification(), new ArrayList<>(), userIdentification);
    JobSpecification jobSpec = compile(job.getId(), jdfFilePath);
    JDFUtil.removeEmptySpaceFromVariables(jobSpec);
    String externalOAuthToken = getAccessTokenByOwnerUsername(userIdentification);

    return buildJobFromJDFFile(job, jdfFilePath, jobSpec, userIdentification, externalOAuthToken);
  }

  private JobSpecification compile(String jobId, String jdfFilePath) throws CompilerException {
    CommonCompiler commonCompiler = new CommonCompiler();
    LOGGER.debug("Job " + jobId + " compilation started at time: " + System.currentTimeMillis());
    commonCompiler.compile(jdfFilePath, FileType.JDF);
    LOGGER.debug("Job " + jobId + " compilation ended at time: " + System.currentTimeMillis());
    return (JobSpecification) commonCompiler.getResult().get(0);
  }

  private JDFJob buildJobFromJDFFile(JDFJob job, String jdfFilePath, JobSpecification jobSpec,
      String userName,
      String externalOAuthToken) {
    try {
      this.jobBuilder.createJobFromJDFFile(job, jdfFilePath, jobSpec,
          userName, externalOAuthToken);

      LOGGER.info("Job [" + job.getId() + "] was built with success at time: " + System
          .currentTimeMillis());

      job.finishCreation();

    } catch (Exception e) {

      LOGGER.error("Failed to build [" + job.getId() + "] : at time: " +
              System.currentTimeMillis(), e);
      job.failCreation();
    }

    return job;
  }

  public ArrayList<JDFJob> getAllJobs(String owner) {
    return (ArrayList<JDFJob>) this.jobDataStore.getAllByOwner(owner);
  }

  public void updateJob(JDFJob job) {
    this.jobDataStore.update(job);
  }

  public String stopJob(String jobId, String owner) {
    // TODO: Stop job at Arrebol
    final int updateCount = this.jobDataStore.deleteByJobId(jobId, owner);

    if (updateCount >= 1) {
      return jobId;
    } else {
      LOGGER.error("Job with id [" + jobId + "] not found in the Datastore.");
      return null;
    }
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

  public User authUser(String credentials) {
    if (credentials == null) {
      LOGGER.error("Invalid credentials. Some of the fields are null.");
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
      this.nonces.remove(credential.getNonce());
      user = this.authenticator.authenticateUser(credential);
    }
    return user;
  }

  public int getNonce() {
    int nonce = UUID.randomUUID().hashCode();
    this.nonces.add(nonce);
    return nonce;
  }

  public User getUser(String username) {
    return this.authenticator.getUserByUsername(username);
  }

  public User addUser(String username, String iguassuToken) {

    try {
      return this.authenticator.addUser(username, iguassuToken);
    } catch (Exception e) {
      throw new RuntimeException("Could not add user", e);
    }
  }

  public JobDataStore getJobDataStore() {
    return this.jobDataStore;
  }

  public void setDataStore(JobDataStore dataStore) {
    this.jobDataStore = dataStore;
  }

  private void validateProperties(Properties properties) throws IguassuException {
    if (properties == null) {
      throw new IllegalArgumentException("Properties cannot be null.");
    } else if (!checkProperties(properties)) {
      throw new IguassuException("Error while initializing Iguassu Controller.");
    }
  }

  public boolean storeOAuthToken(OAuthToken oAuthToken) {
    return this.oAuthTokenDataStore.insert(oAuthToken);
  }

  public List<OAuthToken> getAllOAuthTokens() {
    return this.oAuthTokenDataStore.getAll();
  }

  public String getAccessTokenByOwnerUsername(String ownerUsername) {
    LOGGER.debug("Getting access token of file driver for user " + ownerUsername);

    List<OAuthToken> tokensList = this.oAuthTokenDataStore
        .getAccessTokenByOwnerUsername(ownerUsername);

    String accessToken = null;
    for (OAuthToken token : tokensList) {
      if (!token.hasExpired()) {
        accessToken = token.getAccessToken();
      }
    }

    if (accessToken == null && tokensList.size() != 0) {
      accessToken = refreshExternalOAuthToken(ownerUsername);
    }

    return accessToken;
  }

  private String refreshExternalOAuthToken(String ownerUsername) {
    List<OAuthToken> tokensList = this.oAuthTokenDataStore
        .getAccessTokenByOwnerUsername(ownerUsername);

    String accessToken = null;
    if (tokensList.size() != 0) {
      OAuthToken someToken = tokensList.get(0);
      String someRefreshToken = someToken.getRefreshToken();
      OAuthToken newOAuthToken = this.externalOAuthTokenController.refreshToken(someRefreshToken);
      accessToken = newOAuthToken.getAccessToken();
      deleteTokens(tokensList);
      storeOAuthToken(newOAuthToken);
    }
    return accessToken;
  }

  private void deleteTokens(List<OAuthToken> tokenList) {
    for (OAuthToken token : tokenList) {
      deleteOAuthTokenByAcessToken(token.getAccessToken());
    }
  }

  private void deleteOAuthTokenByAcessToken(String accessToken) {
    this.oAuthTokenDataStore.deleteByAccessToken(accessToken);
  }

  public void removeOAuthTokens(String userId) {
    List<OAuthToken> tokensList = this.oAuthTokenDataStore.getAccessTokenByOwnerUsername(userId);
    deleteTokens(tokensList);
  }
}
