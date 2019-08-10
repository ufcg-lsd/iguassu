package org.fogbowcloud.app.core;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.auth.AuthManager;
import org.fogbowcloud.app.core.auth.DefaultAuthManager;
import org.fogbowcloud.app.core.datastore.managers.JobDBManager;
import org.fogbowcloud.app.core.datastore.managers.UserDBManager;
import org.fogbowcloud.app.core.exceptions.JobNotFoundException;
import org.fogbowcloud.app.core.exceptions.UnauthorizedRequestException;
import org.fogbowcloud.app.core.exceptions.UserNotExistException;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.core.models.job.JobState;
import org.fogbowcloud.app.core.models.user.OAuth2Identifiers;
import org.fogbowcloud.app.core.models.user.OAuthToken;
import org.fogbowcloud.app.core.models.user.RequesterCredential;
import org.fogbowcloud.app.core.models.user.User;
import org.fogbowcloud.app.core.routines.DefaultRoutineManager;
import org.fogbowcloud.app.core.routines.RoutineManager;
import org.fogbowcloud.app.jdfcompiler.job.JobBuilder;
import org.fogbowcloud.app.jdfcompiler.job.JobSpecification;
import org.fogbowcloud.app.jdfcompiler.main.CommonCompiler;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.app.utils.JDFUtil;

import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ApplicationFacade {
    private static final Logger logger = Logger.getLogger(ApplicationFacade.class);

    private static ApplicationFacade instance;
    private final List<Integer> nonceList;
    private final Queue<Job> jobsToSubmit;
    private AuthManager authManager;
    private JobBuilder jobBuilder;
    private JobDBManager jobDBManager;
    private UserDBManager userDBManager;

    private ApplicationFacade() {
        this.jobsToSubmit = new ConcurrentLinkedQueue<>();
        this.nonceList = new ArrayList<>();
        this.jobDBManager = JobDBManager.getInstance();
        this.userDBManager = UserDBManager.getInstance();
    }

    public static ApplicationFacade getInstance() {
        synchronized (ApplicationFacade.class) {
            if (instance == null) {
                instance = new ApplicationFacade();
            }
            return instance;
        }
    }

    public void init(Properties properties) {
        this.authManager = new DefaultAuthManager(properties);
        this.jobBuilder = new JobBuilder(properties);
        final RoutineManager routineManager = new DefaultRoutineManager(properties, this.jobsToSubmit);
        routineManager.startAll();
    }

    public synchronized String submitJob(String jdfFilePath, User jobOwner) throws CompilerException {
        logger.debug("Adding job of user " + jobOwner.getAlias() + " to buffer.");

        final Job job = buildJob(jdfFilePath, jobOwner);
        this.jobDBManager.save(job);
        this.jobsToSubmit.offer(this.jobDBManager.findOne(job.getId()));

        return job.getId();
    }

    private Job buildJob(String jdfFilePath, User owner) throws CompilerException {

        final String ownerAlias = owner.getAlias();
        Job job = new Job(new ArrayList<>(), ownerAlias, owner.getId());

        logger.debug("Building job " + job.getId() + " of user " + owner.getAlias() + " of jdf " + jdfFilePath);
        JobSpecification jobSpec = compile(jdfFilePath);
        JDFUtil.removeEmptySpaceFromVariables(jobSpec);
        OAuthToken oAuthToken = owner.getCredentials().getOauthToken();

        return buildJobFromJDFFile(job, jdfFilePath, jobSpec, ownerAlias,
                Objects.requireNonNull(oAuthToken).getAccessToken(), oAuthToken.getVersion());
    }

    public User authorizeUser(RequesterCredential requesterCredentials) throws GeneralSecurityException, UserNotExistException {
        if (Objects.isNull(requesterCredentials.getIguassuToken())) {
            throw new IllegalArgumentException("Iguassu Token missing.");
        }

        User authenticatedUser;
        logger.info("Checking nonce.");
        if (this.nonceList.contains(requesterCredentials.getNonce())) {
            this.nonceList.remove(requesterCredentials.getNonce());
            authenticatedUser = this.authManager.authorize(requesterCredentials);
        } else {
            throw new GeneralSecurityException("Invalid nonce for this request.");
        }
        return authenticatedUser;
    }

    private JobSpecification compile(String jdfFilePath) throws CompilerException {
        CommonCompiler commonCompiler = new CommonCompiler();
        logger.info(
                "Job " + jdfFilePath + " compilation started at time: " + System.currentTimeMillis());
        commonCompiler.compile(jdfFilePath, CommonCompiler.FileType.JDF);
        logger.info("Job " + jdfFilePath + " compilation ended at time: " + System.currentTimeMillis());
        return (JobSpecification) commonCompiler.getResult().get(0);
    }

    private Job buildJobFromJDFFile(Job job, String jdfFilePath, JobSpecification jobSpec, String userAlias,
                                    String externalOAuthToken, Long tokenVersion) {
        try {
            this.jobBuilder.createJobFromJDFFile(job, jdfFilePath, jobSpec, userAlias, externalOAuthToken, tokenVersion);
            logger.info("JDF [" + jdfFilePath + "] was built with success at time: [" + System.currentTimeMillis() + "]");
            job.setState(JobState.CREATED);
        } catch (Exception e) {
            logger.error("Failed to build [" + job.getId() + "] : at time: [" + System.currentTimeMillis() + "]",
                    e);
            job.setState(JobState.FAILED);
        }

        return job;
    }

    public User authenticateUser(OAuth2Identifiers oAuth2Identifiers, String authorizationCode)
            throws GeneralSecurityException {
        try {
            return this.authManager.authenticate(oAuth2Identifiers, authorizationCode, this.getNonce());
        } catch (Exception gse) {
            throw new GeneralSecurityException(gse.getMessage());
        }
    }

    public int getNonce() {
        final int nonce = UUID.randomUUID().hashCode();
        this.nonceList.add(nonce);
        return nonce;
    }

    public OAuthToken refreshToken(OAuthToken oAuthToken) throws GeneralSecurityException {
        try {
            return this.authManager.refreshOAuth2Token(oAuthToken);
        } catch (Exception e) {
            throw new GeneralSecurityException(e.getMessage());
        }
    }

    public OAuthToken findUserOAuthTokenByAlias(String userAlias) {
        return this.userDBManager.findUserByAlias(userAlias).getCredentials().getOauthToken();
    }

    public Collection<Job> findAllJobsByUserId(Long userId) {
        return this.jobDBManager.findByUserId(userId);
    }

    public String removeJob(String jobId, Long userId) throws UnauthorizedRequestException {
        Job job = this.jobDBManager.findOne(jobId);
        if (match(job, userId)) {
            job.setState(JobState.REMOVED);
            this.jobDBManager.save(job);
        } else {
            throw new UnauthorizedRequestException("User with id [" + userId + "] does not own this job.");
        }

        return job.getId();
    }

    public Job findJobById(String jobId, User user) throws JobNotFoundException, UnauthorizedRequestException {
        Job job;
        try {
            job = Objects.requireNonNull(this.jobDBManager.findOne(jobId));
        } catch (Exception e) {
            logger.error("Could not find job with id [" + jobId + "].");
            throw new JobNotFoundException("Could not find job with id [" + jobId + "].");
        }

        if (!match(job, user.getId())) {
            throw new UnauthorizedRequestException("User with id [" + user.getId() + "] does not own this job.");
        }
        return job;
    }

    private boolean match(Job job, Long userId) {
        return job.getOwnerId().equals(userId);
    }
}
