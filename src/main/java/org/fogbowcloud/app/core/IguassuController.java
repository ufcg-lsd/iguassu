package org.fogbowcloud.app.core;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.api.http.services.AuthService;
import org.fogbowcloud.app.core.authenticator.CommonAuthenticator;
import org.fogbowcloud.app.core.authenticator.IguassuAuthenticator;
import org.fogbowcloud.app.core.authenticator.models.Credential;
import org.fogbowcloud.app.core.authenticator.models.User;
import org.fogbowcloud.app.core.constants.IguassuPropertiesConstants;
import org.fogbowcloud.app.core.datastore.JobDataStore;
import org.fogbowcloud.app.core.datastore.OAuthToken;
import org.fogbowcloud.app.core.datastore.OAuthTokenDataStore;
import org.fogbowcloud.app.core.exceptions.IguassuException;
import org.fogbowcloud.app.core.monitor.JobStateMonitor;
import org.fogbowcloud.app.core.monitor.SessionMonitor;
import org.fogbowcloud.app.core.task.Task;
import org.fogbowcloud.app.external.ExternalOAuthConstants;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.job.JDFJobBuilder;
import org.fogbowcloud.app.jdfcompiler.job.JobSpecification;
import org.fogbowcloud.app.jdfcompiler.main.CommonCompiler;
import org.fogbowcloud.app.jdfcompiler.main.CommonCompiler.FileType;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.app.jes.JobExecutionSystem;
import org.fogbowcloud.app.jes.arrebol.ArrebolJobExecutionSystem;
import org.fogbowcloud.app.jes.arrebol.ArrebolJobSynchronizer;
import org.fogbowcloud.app.utils.JDFUtil;
import org.fogbowcloud.app.utils.ManagerTimer;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

public class IguassuController {

    private static final Logger LOGGER = Logger.getLogger(IguassuController.class);

    private static ManagerTimer executionMonitorTimer = new ManagerTimer(
        Executors.newScheduledThreadPool(1));
    private static ManagerTimer sessionMonitorTime = new ManagerTimer(
        Executors.newScheduledThreadPool(1));

    private final Properties properties;
    private final IguassuAuthenticator authenticator;
    private final JobExecutionSystem jobExecutionSystem;
    private List<Integer> nonces;
    private JobDataStore jobDataStore;
    private OAuthTokenDataStore oAuthTokenDataStore;
    private JDFJobBuilder jobBuilder;

    @Autowired
    private AuthService authService;

    public IguassuController(Properties properties) throws IguassuException {
        validateProperties(properties);
        this.properties = properties;
        this.authenticator = new CommonAuthenticator();
        this.jobExecutionSystem = new ArrebolJobExecutionSystem(this.properties);
    }

    private static String requiredPropertyMessage(String property) {
        return "Required property " + property + " was not set";
    }

    private static boolean checkProperties(Properties properties) {
        if (!properties.containsKey(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_CLIENT_ID)) {
            LOGGER.error(
                requiredPropertyMessage(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_CLIENT_ID));
            return false;
        }

        if (!properties.containsKey(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_CLIENT_SECRET)) {
            LOGGER.error(
                requiredPropertyMessage(
                    ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_CLIENT_SECRET));
            return false;
        }

        if (!properties.containsKey(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_TOKEN_URL)) {
            LOGGER.error(
                requiredPropertyMessage(ExternalOAuthConstants.OAUTH_STORAGE_SERVICE_TOKEN_URL));
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
                this.properties.getProperty(IguassuPropertiesConstants.DATABASE_HOST_URL));
        this.oAuthTokenDataStore =
            new OAuthTokenDataStore(
                this.properties.getProperty(IguassuPropertiesConstants.DATABASE_HOST_URL));
        this.jobBuilder = new JDFJobBuilder(this.properties);
        this.nonces = new ArrayList<>();

        this.initMonitors();
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

        LOGGER.info(
            "Iguassu Job [" + job.getId() + "] has arrebol id: [" + job.getJobIdArrebol() + "]");

        this.jobDataStore.insert(job);

        return job.getId();
    }

    public JDFJob buildJob(String jdfFilePath, User owner) throws CompilerException {
        String userIdentification = owner.getUserIdentification();
        JDFJob job = new JDFJob(owner.getUserIdentification(), new ArrayList<>(),
            userIdentification);
        JobSpecification jobSpec = compile(job.getId(), jdfFilePath);
        JDFUtil.removeEmptySpaceFromVariables(jobSpec);
        OAuthToken oAuthToken = null;
        try {
            oAuthToken = getCurrentTokenByUserId(userIdentification);
            if (oAuthToken.hasExpired()) {
                oAuthToken = this.authService.refreshAndDelete(oAuthToken);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return buildJobFromJDFFile(job, jdfFilePath, jobSpec, userIdentification,
            Objects.requireNonNull(oAuthToken).getAccessToken(), oAuthToken.getVersion());
    }

    private void initMonitors() {

        final long JOB_MONITOR_INITIAL_DELAY = 3000;
        final long JOB_MONITOR_EXECUTION_PERIOD = 5000;

        JobStateMonitor jobStateMonitor = new JobStateMonitor(this.jobDataStore,
            new ArrebolJobSynchronizer(this.properties));
        executionMonitorTimer.scheduleAtFixedRate(jobStateMonitor, JOB_MONITOR_INITIAL_DELAY,
            JOB_MONITOR_EXECUTION_PERIOD);

        final long SESSION_MONITOR_INITIAL_DELAY = 3000;
        final long SESSION_MONITOR_EXECUTION_PERIOD = 3600000;

        SessionMonitor sessionMonitor = new SessionMonitor(this.oAuthTokenDataStore,
            this.authenticator);
        sessionMonitorTime.scheduleAtFixedRate(sessionMonitor, SESSION_MONITOR_INITIAL_DELAY,
            SESSION_MONITOR_EXECUTION_PERIOD);
    }

    private JobSpecification compile(String jobId, String jdfFilePath) throws CompilerException {
        CommonCompiler commonCompiler = new CommonCompiler();
        LOGGER
            .debug("Job " + jobId + " compilation started at time: " + System.currentTimeMillis());
        commonCompiler.compile(jdfFilePath, FileType.JDF);
        LOGGER.debug("Job " + jobId + " compilation ended at time: " + System.currentTimeMillis());
        return (JobSpecification) commonCompiler.getResult().get(0);
    }

    private JDFJob buildJobFromJDFFile(JDFJob job, String jdfFilePath, JobSpecification jobSpec,
        String userName,
        String externalOAuthToken, Long tokenVersion) {
        try {
            this.jobBuilder.createJobFromJDFFile(job, jdfFilePath, jobSpec,
                userName, externalOAuthToken, tokenVersion);

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

    public User authUser(String credentials) throws GeneralSecurityException {
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
            user = this.authenticator.authorizesUser(credential);
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

    public OAuthToken getCurrentTokenByUserId(String userId) {
        OAuthToken oAuthToken = null;
        List<OAuthToken> oAuthTokens = this.oAuthTokenDataStore
            .getAccessTokenByOwnerUsername(userId);
        for (OAuthToken t : oAuthTokens) {
            if (oAuthToken == null || oAuthToken.getVersion() < t.getVersion()) {
                oAuthToken = t;
            }
        }
        return oAuthToken;
    }

    public boolean deleteOAuthToken(OAuthToken oAuthToken) {
        return this.oAuthTokenDataStore.deleteByAccessToken(oAuthToken.getAccessToken());
    }
}
