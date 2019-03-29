package org.fogbowcloud.app.core;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.datastore.JobDataStore;
import org.fogbowcloud.app.core.datastore.OAuthTokenDataStore;
import org.fogbowcloud.app.core.exceptions.IguassuException;
import org.fogbowcloud.app.external.ExternalOAuthController;
import org.fogbowcloud.app.jdfcompiler.job.JobSpecification;
import org.fogbowcloud.app.jdfcompiler.main.CommonCompiler;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.app.jdfcompiler.main.CommonCompiler.FileType;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.job.JDFJobBuilder;
import org.fogbowcloud.app.core.datastore.OAuthToken;
import org.fogbowcloud.app.core.authenticator.models.User;
import org.fogbowcloud.app.core.constants.IguassuPropertiesConstants;
import org.fogbowcloud.app.core.authenticator.IguassuAuthenticator;
import org.fogbowcloud.app.core.authenticator.models.Credential;
import org.fogbowcloud.app.core.authenticator.ThirdAppAuthenticator;
import org.fogbowcloud.blowout.core.constants.FogbowConstants;
import org.fogbowcloud.blowout.core.BlowoutController;
import org.fogbowcloud.blowout.core.exception.BlowoutException;
import org.fogbowcloud.blowout.core.model.task.Task;
import org.fogbowcloud.blowout.core.model.task.TaskState;
import org.fogbowcloud.blowout.core.constants.AppPropertiesConstants;
import org.fogbowcloud.blowout.core.util.ManagerTimer;
import org.json.JSONException;
import org.json.JSONObject;


public class IguassuController {

    private static final Logger LOGGER = Logger.getLogger(IguassuController.class);

    private final Properties properties;
    private BlowoutController blowoutController;
    private List<Integer> nonces;
    private Map<String, Task> finishedTasks;
    private Map<String, Thread> createdJobs;
    private JobDataStore jobDataStore;
    private OAuthTokenDataStore oAuthTokenDataStore;
    private IguassuAuthenticator auth;
    private ExternalOAuthController externalOAuthTokenController;

    private static ManagerTimer executionMonitorTimer = new ManagerTimer(Executors.newScheduledThreadPool(1));

    public IguassuController(Properties properties)
            throws BlowoutException, IguassuException {
        validateProperties(properties);
        this.properties = properties;
        this.finishedTasks = new ConcurrentHashMap<>();
        this.blowoutController = new BlowoutController(properties);
        this.createdJobs = new ConcurrentHashMap<>();
        this.externalOAuthTokenController = new ExternalOAuthController(properties);
        this.auth = new ThirdAppAuthenticator(this.properties);
    }

    public Properties getProperties() {
        return this.properties;
    }

    public void init() throws Exception {
        this.jobDataStore = new JobDataStore(this.properties.getProperty(AppPropertiesConstants.DB_DATASTORE_URL));
        this.oAuthTokenDataStore = new OAuthTokenDataStore(this.properties.getProperty(AppPropertiesConstants.DB_DATASTORE_URL));

        boolean removePreviousResources = Boolean.parseBoolean(this.properties.getProperty(IguassuPropertiesConstants.REMOVE_PREVIOUS_RESOURCES));

        this.blowoutController.start(removePreviousResources);

        LOGGER.info("Default Compute flavor specification: " + this.properties.getProperty(IguassuPropertiesConstants.DEFAULT_COMPUTE_FLAVOR_SPEC));

        this.nonces = new ArrayList<>();

        LOGGER.debug("Restarting jobs");
        restartAllJobs();

        int schedulerMonitorPeriod = Integer.valueOf(this.properties.getProperty(IguassuPropertiesConstants.EXECUTION_MONITOR_PERIOD));

        LOGGER.debug("Starting Execution Monitor, with period: " + schedulerMonitorPeriod);
        ExecutionMonitorWithDB executionMonitor = new ExecutionMonitorWithDB(this, this.jobDataStore);
        executionMonitorTimer.scheduleAtFixedRate(executionMonitor, 0, schedulerMonitorPeriod);
    }

    public void stop() {
        for (Thread t : createdJobs.values()) {
            while (t.isAlive())  t.interrupt();
        }
    }

    public void restartAllJobs() throws BlowoutException {
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
                            .getRequirementValue(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS));
                } else {
                    finishedTasks.put(task.getId(), task);
                }
            }
            blowoutController.addTaskList(taskList);
        }
    }

    public JDFJob getJobById(String jobId, String owner) {
        return this.jobDataStore.getByJobId(jobId, owner);
    }

    public String addJob(String jdfFilePath, User owner)
            throws CompilerException, NameAlreadyInUseException {
        LOGGER.debug("Adding job  of owner " + owner.getUsername() + " to scheduler");
        JDFJob job = runJobFromJDFFile(jdfFilePath, owner);
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
        Thread t = new Thread(new AsyncJobBuilder(job, jdfFilePath, this.properties,
                this.blowoutController, this.jobDataStore, jobSpec, userName, externalOAuthToken),"Thread with Job " + job.getId());
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
            this.blowoutController.cleanTasks(jobToRemove.getTasks());
            /*
            for (Task task : jobToRemove.getTasks()) {
                LOGGER.info("Removing task " + task.getId() + " from job.");
                this.blowoutController.cleanTask(task);
            }
            */
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
                task.getMetadata(IguassuPropertiesConstants.JOB_ID),
                task.getMetadata(IguassuPropertiesConstants.OWNER)
        );
        LOGGER.info("Moving task " + task.getId() + " from job " + job.getName() + " to finished");
        this.finishedTasks.put(task.getId(), task);
        job.finish(task);
        updateJob(job);
        this.blowoutController.cleanTask(task);
    }

    public User authUser(String credentials) {
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
            this.nonces.remove(credential.getNonce());
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
        if (!properties.containsKey(IguassuPropertiesConstants.IGUASSU_PUBLIC_KEY)) {
            LOGGER.error(requiredPropertyMessage(IguassuPropertiesConstants.IGUASSU_PUBLIC_KEY));
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
                LOGGER.info("Submitted " + job.getId() + " to blowout at time: " + System.currentTimeMillis());
                this.job.finishCreation();
            } catch (Exception e) {
                LOGGER.error("Failed to Submit " + job.getId() + " to blowout at time: " + System.currentTimeMillis(), e);
                this.job.failCreation();
            }
            this.db.update(job);
        }
    }

}
