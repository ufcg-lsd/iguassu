package org.fogbowcloud.app.core;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.auth.AuthManager;
import org.fogbowcloud.app.core.auth.DefaultAuthManager;
import org.fogbowcloud.app.core.constants.ConfProperty;
import org.fogbowcloud.app.core.models.auth.Credential;
import org.fogbowcloud.app.core.models.auth.OAuth2Identifiers;
import org.fogbowcloud.app.core.models.auth.OAuthToken;
import org.fogbowcloud.app.core.models.auth.User;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.core.models.job.JobSpecification;
import org.fogbowcloud.app.core.models.task.Task;
import org.fogbowcloud.app.core.routines.DefaultRoutineManager;
import org.fogbowcloud.app.core.routines.RoutineManager;
import org.fogbowcloud.app.jdfcompiler.JDFJobBuilder;
import org.fogbowcloud.app.jdfcompiler.main.CommonCompiler;
import org.fogbowcloud.app.jdfcompiler.main.CommonCompiler.FileType;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.app.utils.JDFUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class IguassuController {

    private static final Logger logger = Logger.getLogger(IguassuController.class);

    private final Properties properties;
    private final AuthManager authManager;
    private final List<Integer> nonceList;
    private final JDFJobBuilder jobBuilder;
    private final Queue<Job> jobsToSubmit;

    public IguassuController(Properties properties) {
        this.properties = properties;
        this.jobsToSubmit = new ConcurrentLinkedQueue<>();
        this.authManager = new DefaultAuthManager(this.properties, this.oAuthTokenDataStore);
        this.jobBuilder = new JDFJobBuilder(this.properties);
        this.nonceList = new ArrayList<>();
    }

    public void init() {
        final RoutineManager routineManager =
                new DefaultRoutineManager(
                        this.properties,
                        this.authManager,
                        this.jobsToSubmit);
        routineManager.startAll();
    }

    Job getJobById(String jobId, String user) {
        return this.jobDataStore.getByJobId(jobId, user);
    }

    void updateUser(User user) {
        this.authManager.update(user);
    }

    String submitJob(String jdfFilePath, User user) throws CompilerException {
        logger.debug("Adding job of user " + user.getIdentifier() + " to buffer.");

        final Job job = buildJob(jdfFilePath, user);
        this.jobsToSubmit.offer(job);
        this.jobDataStore.insert(job);

        return job.getId();
    }

    Job buildJob(String jdfFilePath, User user) throws CompilerException {

        final String userIdentification = user.getIdentifier();
        final String jobId = UUID.randomUUID().toString();
        Job job = new Job(jobId, new ArrayList<>(), userIdentification);

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

    ArrayList<Job> getAllJobs(String userId) {
        return (ArrayList<Job>) this.jobDataStore.getAllByUserId(userId);
    }

    void updateJob(Job job) {
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
        for (Job job : getAllJobs(userId)) {
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

        User authenticatedUser = null;
        logger.debug("Checking nonce.");
        if (this.nonceList.contains(credential.getNonce())) {
            this.nonceList.remove(credential.getNonce());
            authenticatedUser = this.authManager.authorize(credential);
        }
        return authenticatedUser;
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

    private Job buildJobFromJDFFile(
            Job job,
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
