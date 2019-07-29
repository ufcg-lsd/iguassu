package org.fogbowcloud.app.core;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.auth.AuthManager;
import org.fogbowcloud.app.core.auth.DefaultAuthManager;
import org.fogbowcloud.app.core.auth.models.Credential;
import org.fogbowcloud.app.core.auth.models.OAuth2Identifiers;
import org.fogbowcloud.app.core.auth.models.OAuthToken;
import org.fogbowcloud.app.core.auth.models.User;
import org.fogbowcloud.app.core.constants.ConfProperty;
import org.fogbowcloud.app.core.datastore.JobDataStore;
import org.fogbowcloud.app.core.datastore.OAuthTokenDataStore;
import org.fogbowcloud.app.core.routines.DefaultRoutineManager;
import org.fogbowcloud.app.core.routines.RoutineManager;
import org.fogbowcloud.app.core.task.Task;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.job.JDFJobBuilder;
import org.fogbowcloud.app.jdfcompiler.job.JobSpecification;
import org.fogbowcloud.app.jdfcompiler.main.CommonCompiler;
import org.fogbowcloud.app.jdfcompiler.main.CommonCompiler.FileType;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.app.utils.JDFUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class IguassuController {

    private static final Logger logger = Logger.getLogger(IguassuController.class);

    private final Properties properties;
    private final AuthManager authManager;
    private final List<Integer> nonceList;
    private final JobDataStore jobDataStore;
    private final OAuthTokenDataStore oAuthTokenDataStore;
    private final JDFJobBuilder jobBuilder;
    private final Queue<JDFJob> jobsToSubmit;

    public IguassuController(Properties properties) {
        this.properties = properties;
        this.jobsToSubmit = new ConcurrentLinkedQueue<>();
        this.jobDataStore =
                new JobDataStore(
                        this.properties.getProperty(ConfProperty.DATABASE_HOST_URL.getProp()));
        this.oAuthTokenDataStore =
                new OAuthTokenDataStore(
                        this.properties.getProperty(ConfProperty.DATABASE_HOST_URL.getProp()));
        this.authManager = new DefaultAuthManager(this.properties, this.oAuthTokenDataStore);
        this.jobBuilder = new JDFJobBuilder(this.properties);
        this.nonceList = new ArrayList<>();
    }

    public void init() {
        final RoutineManager routineManager =
                new DefaultRoutineManager(
                        this.properties,
                        this.oAuthTokenDataStore,
                        this.jobDataStore,
                        this.authManager,
                        this.jobsToSubmit);
        routineManager.startAll();
    }

    JDFJob getJobById(String jobId, String user) {
        return this.jobDataStore.getByJobId(jobId, user);
    }

    void updateUser(User user) {
        this.authManager.update(user);
    }

    String submitJob(String jdfFilePath, User user) throws CompilerException {
        logger.debug("Adding job of user " + user.getIdentifier() + " to buffer.");

        final JDFJob job = buildJob(jdfFilePath, user);
        this.jobsToSubmit.offer(job);
        this.jobDataStore.insert(job);

        return job.getId();
    }

    JDFJob buildJob(String jdfFilePath, User user) throws CompilerException {

        final String userIdentification = user.getIdentifier();
        final String jobId = UUID.randomUUID().toString();
        JDFJob job = new JDFJob(jobId, new ArrayList<>(), userIdentification);

        logger.debug("Building job " + job.getId() + " of user " + user.getIdentifier());
        JobSpecification jobSpec = compile(job.getId(), jdfFilePath);
        JDFUtil.removeEmptySpaceFromVariables(jobSpec);
        OAuthToken oAuthToken = null;
        try {
            oAuthToken = getCurrentTokenByUserId(userIdentification);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return buildJobFromJDFFile(
                job,
                jdfFilePath,
                jobSpec,
                userIdentification,
                Objects.requireNonNull(oAuthToken).getAccessToken(),
                oAuthToken.getVersion());
    }

    ArrayList<JDFJob> getAllJobs(String userId) {
        return (ArrayList<JDFJob>) this.jobDataStore.getAllByUserId(userId);
    }

    void updateJob(JDFJob job) {
        this.jobDataStore.update(job);
    }

    String stopJob(String jobId, String userId) {
        final int updateCount = this.jobDataStore.deleteByJobId(jobId, userId);

        if (updateCount >= 1) {
            return jobId;
        } else {
            logger.error("Job with id [" + jobId + "] not found in the Datastore.");
            return null;
        }
    }

    Task getTaskById(String taskId, String userId) {
        for (JDFJob job : getAllJobs(userId)) {
            Task task = job.getTaskById(taskId);
            if (task != null) {
                return task;
            }
        }
        return null;
    }

    User authorizeUser(String credentials) throws GeneralSecurityException {
        if (Objects.isNull(credentials)) {
            logger.error("Invalid credentials. The fields are null.");
            return null;
        }

        Credential credential;
        try {
            JSONObject jsonObject = new JSONObject(credentials);
            credential = Credential.fromJSON(jsonObject);
        } catch (JSONException e) {
            logger.error("Invalid credentials format.", e);
            return null;
        }

        User user = null;
        logger.debug("Checking nonce.");
        if (this.nonceList.contains(credential.getNonce())) {
            this.nonceList.remove(credential.getNonce());
            user = this.authManager.authorize(credential);
        }
        return user;
    }

    int getNonce() {
        final int nonce = UUID.randomUUID().hashCode();
        this.nonceList.add(nonce);
        return nonce;
    }

    JobDataStore getJobDataStore() {
        return this.jobDataStore;
    }

    void storeOAuthToken(OAuthToken oAuthToken) {
        this.oAuthTokenDataStore.insert(oAuthToken);
    }

    OAuthToken getCurrentTokenByUserId(String userId) {
        return this.oAuthTokenDataStore.getCurrentTokenByUserId(userId);
    }

    void deleteOAuthToken(OAuthToken oAuthToken) {
        this.oAuthTokenDataStore.deleteByAccessToken(oAuthToken.getAccessToken());
    }

    private JobSpecification compile(String jobId, String jdfFilePath) throws CompilerException {
        CommonCompiler commonCompiler = new CommonCompiler();
        logger.debug(
                "Job " + jobId + " compilation started at time: " + System.currentTimeMillis());
        commonCompiler.compile(jdfFilePath, FileType.JDF);
        logger.debug("Job " + jobId + " compilation ended at time: " + System.currentTimeMillis());
        return (JobSpecification) commonCompiler.getResult().get(0);
    }

    private JDFJob buildJobFromJDFFile(
            JDFJob job,
            String jdfFilePath,
            JobSpecification jobSpec,
            String userId,
            String externalOAuthToken,
            Long tokenVersion) {
        try {
            this.jobBuilder.createJobFromJDFFile(
                    job, jdfFilePath, jobSpec, userId, externalOAuthToken, tokenVersion);

            logger.info(
                    "Job ["
                            + job.getId()
                            + "] was built with success at time: "
                            + System.currentTimeMillis());

            job.finishCreation();

        } catch (Exception e) {

            logger.error(
                    "Failed to build ["
                            + job.getId()
                            + "] : at time: "
                            + System.currentTimeMillis(),
                    e);
            job.failCreation();
        }

        return job;
    }

    User authenticateUser(OAuth2Identifiers oAuth2Identifiers, String authorizationCode)
            throws GeneralSecurityException {
        try {
            return this.authManager.authenticate(oAuth2Identifiers, authorizationCode);
        } catch (Exception gse) {
            throw new GeneralSecurityException(gse.getMessage());
        }
    }

    OAuthToken refreshToken(OAuthToken oAuthToken) throws GeneralSecurityException {
        try {
            return this.authManager.refreshOAuth2Token(oAuthToken);
        } catch (Exception e) {
            throw new GeneralSecurityException(e.getMessage());
        }
    }
}
