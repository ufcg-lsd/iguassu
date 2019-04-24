package org.fogbowcloud.app.core;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.arrebol.ArrebolFacade;
import org.fogbowcloud.app.core.constants.FogbowConstants;
import org.fogbowcloud.app.core.constants.IguassuGeneralConstants;
import org.fogbowcloud.app.core.datastore.JobDataStore;
import org.fogbowcloud.app.core.datastore.OAuthTokenDataStore;
import org.fogbowcloud.app.core.exceptions.IguassuException;
import org.fogbowcloud.app.core.task.Task;
import org.fogbowcloud.app.core.task.TaskState;
import org.fogbowcloud.app.external.ExternalOAuthController;
import org.fogbowcloud.app.jdfcompiler.job.AsyncJobBuilder;
import org.fogbowcloud.app.jdfcompiler.job.JDFJobState;
import org.fogbowcloud.app.jdfcompiler.job.JobSpecification;
import org.fogbowcloud.app.jdfcompiler.main.CommonCompiler;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.app.jdfcompiler.main.CommonCompiler.FileType;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.core.datastore.OAuthToken;
import org.fogbowcloud.app.core.authenticator.models.User;
import org.fogbowcloud.app.core.constants.IguassuPropertiesConstants;
import org.fogbowcloud.app.core.authenticator.IguassuAuthenticator;
import org.fogbowcloud.app.core.authenticator.models.Credential;
import org.fogbowcloud.app.core.authenticator.ThirdAppAuthenticator;
import org.fogbowcloud.app.utils.ManagerTimer;
import org.json.JSONException;
import org.json.JSONObject;

public class IguassuController {

    private static final Logger LOGGER = Logger.getLogger(IguassuController.class);

    private final Properties properties;
    private List<Integer> nonces;
    private Map<String, Task> finishedTasks;
    private Map<String, Thread> createdJobs;
    private JobDataStore jobDataStore;
    private OAuthTokenDataStore oAuthTokenDataStore;
    private IguassuAuthenticator authenticator;
    private ExternalOAuthController externalOAuthTokenController;
    private ArrebolFacade arrebolFacade;

    private static ManagerTimer executionMonitorTimer = new ManagerTimer(Executors.newScheduledThreadPool(1));

    public IguassuController(Properties properties)
            throws IguassuException {
        validateProperties(properties);
        this.properties = properties;
        this.finishedTasks = new ConcurrentHashMap<>();
        this.createdJobs = new ConcurrentHashMap<>();
        this.externalOAuthTokenController = new ExternalOAuthController(properties);
        this.authenticator = new ThirdAppAuthenticator(this.properties);
        this.arrebolFacade = new ArrebolFacade();
    }

    public Properties getProperties() {
        return this.properties;
    }

    public void init() {
        this.jobDataStore = new JobDataStore(this.properties.getProperty(IguassuGeneralConstants.DB_DATASTORE_URL));
        this.oAuthTokenDataStore = new OAuthTokenDataStore(this.properties.getProperty(IguassuGeneralConstants.DB_DATASTORE_URL));

        this.nonces = new ArrayList<>();

        final int localJobsMonitorPeriod = Integer.valueOf(this.properties.getProperty(
                IguassuPropertiesConstants.EXECUTION_MONITOR_PERIOD));

        LOGGER.debug("Starting Execution Monitor, with period: " + localJobsMonitorPeriod);
        ExecutionMonitorWithDB executionMonitor = new ExecutionMonitorWithDB(this, this.jobDataStore);
        executionMonitorTimer.scheduleAtFixedRate(executionMonitor, 0, localJobsMonitorPeriod);
    }

    public void restartAllJobs()  {
        for (JDFJob job : this.jobDataStore.getAll()) {
            if (job.getState().equals(JDFJobState.SUBMITTED)) {
                job.failCreation();
                this.jobDataStore.update(job);
            }
            ArrayList<Task> taskList = new ArrayList<>();
            for (Task task : job.getTasks()) {
                if (!task.isFinished()) {
                    taskList.add(task);
                    LOGGER.debug("Specification of Recovered task: " + task.getSpecification().toJSON().toString());
                    LOGGER.debug("Task Requirements: " + task.getSpecification()
                            .getRequirementValue(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS));
                } else {
                    finishedTasks.put(task.getId(), task);
                }
            }
        }
    }

    public JDFJob getJobById(String jobId, String owner) {
        return this.jobDataStore.getByJobId(jobId, owner);
    }

    public String addJob(String jdfFilePath, User owner)
            throws CompilerException {
        LOGGER.debug("Adding job  of owner " + owner.getUsername() + " to scheduler");
        JDFJob job = runJobFromJDFFile(jdfFilePath, owner);
        jobDataStore.insert(job);
        return job.getId();
    }

    public JDFJob runJobFromJDFFile(String jdfFilePath, User owner) throws CompilerException {
        String userName = owner.getUsername();
        JDFJob job = new JDFJob(owner.getUser(), new ArrayList<>(), userName);
        JobSpecification jobSpec = compile(job.getId(), jdfFilePath);

        String externalOAuthToken = getAccessTokenByOwnerUsername(userName);

        Thread t = runNewJobThread(job, jdfFilePath,jobSpec, userName, externalOAuthToken);

        this.createdJobs.put(job.getId(), t);
        return job;
    }

    private JobSpecification compile(String jobId, String jdfFilePath) throws CompilerException {
        CommonCompiler commonCompiler = new CommonCompiler();
        LOGGER.debug("Job " + jobId + " compilation started at time: " + System.currentTimeMillis());
        commonCompiler.compile(jdfFilePath, FileType.JDF);
        LOGGER.debug("Job " + jobId + " compilation ended at time: " + System.currentTimeMillis());
        JobSpecification jobSpec = (JobSpecification) commonCompiler.getResult().get(0);
        return jobSpec;
    }

    private Thread runNewJobThread(JDFJob job, String jdfFilePath, JobSpecification jobSpec, String userName, String externalOAuthToken) {
        Thread t = new Thread(new AsyncJobBuilder(job, jdfFilePath, this.properties, this.jobDataStore, jobSpec,
                userName, externalOAuthToken, this.arrebolFacade),"Thread with Job " + job.getId());
        LOGGER.debug("Thread " + t.getId() + " is in state: " + t.getState() + " with job: " + t.getName());
        t.start();
        LOGGER.debug("Thread " + t.getId() + "with job" + t.getName() + " started");
        return t;
    }

    public void waitForJobCreation(String jobId) throws InterruptedException {
        createdJobs.get(jobId).join();
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
            Thread creatingThread = this.createdJobs.get(jobToRemove.getId());
            if (creatingThread != null) {
                if (creatingThread.isAlive()) {
                    LOGGER.info("Job was still being created.");
                    while (creatingThread.isAlive()) {
                        creatingThread.interrupt();
                    }
                }
                this.createdJobs.remove(jobToRemove.getId());
            }
            this.jobDataStore.deleteByJobId(jobToRemove.getId(), owner);

            LOGGER.info("Removing Job " + jobToRemove.getId());
            // Call API to delete a job
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
        return finishedTasks.get(taskId).getState();
    }

    public void moveTaskToFinished(Task task) {
        JDFJob job = this.jobDataStore.getByJobId(
                task.getMetadata(IguassuPropertiesConstants.JOB_ID),
                task.getMetadata(IguassuPropertiesConstants.OWNER)
        );
        LOGGER.info("Moving task " + task.getId() + " from job " + job.getName() + " to finished");
        this.finishedTasks.put(task.getId(), task);
        updateJob(job);
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

    public User addUser(String username, String publicKey) {
        try {
            return this.authenticator.addUser(username, publicKey);
        } catch (Exception e) {
            throw new RuntimeException("Could not add user", e);
        }
    }

    public String getAuthenticatorName() {
        return this.authenticator.getAuthenticatorName();
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

    private static String requiredPropertyMessage(String property) {
        return "Required property " + property + " was not set";
    }

    private static boolean checkProperties(Properties properties) {
        // Required properties
        if (!properties.containsKey(IguassuPropertiesConstants.EXECUTION_MONITOR_PERIOD)) {
            LOGGER.error(requiredPropertyMessage(IguassuPropertiesConstants.EXECUTION_MONITOR_PERIOD));
            return false;
        }
        if (!properties.containsKey(IguassuPropertiesConstants.IGUASSU_PRIVATE_KEY_FILEPATH)) {
            LOGGER.error(requiredPropertyMessage(IguassuPropertiesConstants.IGUASSU_PRIVATE_KEY_FILEPATH));
            return false;
        }
        if (properties.containsKey(IguassuPropertiesConstants.ENCRYPTION_TYPE)) {
            try {
                MessageDigest.getInstance(properties.getProperty(IguassuPropertiesConstants.ENCRYPTION_TYPE));
            } catch (NoSuchAlgorithmException e) {
                String builder = "Property " +
                        IguassuPropertiesConstants.ENCRYPTION_TYPE +
                        "(" +
                        properties.getProperty(IguassuPropertiesConstants.ENCRYPTION_TYPE) +
                        ") does not refer to a valid encryption algorithm." +
                        " Valid options are 'MD5', 'SHA-1' and 'SHA-256'.";
                LOGGER.error(builder);
                return false;
            }
        }
        LOGGER.debug("All properties are set");
        return true;
    }

    public boolean storeOAuthToken(OAuthToken oAuthToken) {
        return this.oAuthTokenDataStore.insert(oAuthToken);
    }

    public List<OAuthToken> getAllOAuthTokens() {
        return this.oAuthTokenDataStore.getAll();
    }

    public String getAccessTokenByOwnerUsername(String ownerUsername) {
        LOGGER.debug("Getting access token of file driver for user " + ownerUsername);

        List<OAuthToken> tokensList = this.oAuthTokenDataStore.getAccessTokenByOwnerUsername(ownerUsername);

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

    public void deleteOAuthTokenByAcessToken(String accessToken) {
        this.oAuthTokenDataStore.deleteByAccessToken(accessToken);
    }

    public void deleteAllExternalOAuthTokens() {
        this.oAuthTokenDataStore.deleteAll();
    }

    private String refreshExternalOAuthToken(String ownerUsername) {
        List<OAuthToken> tokensList = this.oAuthTokenDataStore.getAccessTokenByOwnerUsername(ownerUsername);

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
}
