package org.fogbowcloud.app.core;

import static org.fogbowcloud.app.core.datastore.managers.QueueDBManager.DEFAULT_QUEUE_ID;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.api.dtos.ResourceDTOResponse;
import org.fogbowcloud.app.api.dtos.ResourceNode;
import org.fogbowcloud.app.api.dtos.QueueDTORequest;
import org.fogbowcloud.app.core.auth.AuthManager;
import org.fogbowcloud.app.core.auth.DefaultAuthManager;
import org.fogbowcloud.app.core.datastore.managers.JobDBManager;
import org.fogbowcloud.app.core.datastore.managers.QueueDBManager;
import org.fogbowcloud.app.core.datastore.managers.UserDBManager;
import org.fogbowcloud.app.core.exceptions.JobNotFoundException;
import org.fogbowcloud.app.core.exceptions.UnauthorizedRequestException;
import org.fogbowcloud.app.core.exceptions.UserNotExistException;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.core.models.job.JobState;
import org.fogbowcloud.app.core.models.queue.ArrebolQueue;
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
import org.fogbowcloud.app.jes.arrebol.dtos.QueueDTO;
import org.fogbowcloud.app.jes.arrebol.helpers.QueueRequestHelper;
import org.fogbowcloud.app.jes.exceptions.QueueNotFoundException;
import org.fogbowcloud.app.ps.ProvisioningRequestHelper;
import org.fogbowcloud.app.ps.ResourceProvideThread;
import org.fogbowcloud.app.ps.models.Pool;
import org.fogbowcloud.app.utils.JDFUtil;
import org.fogbowcloud.app.utils.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class ApplicationFacade {

    private static final Logger logger = Logger.getLogger(ApplicationFacade.class);

    private static ApplicationFacade instance;
    private final List<Integer> nonceList;
    private final Queue<Pair<String, Job>> jobsToSubmit;
    private QueueRequestHelper queueRequestHelper;
    private ProvisioningRequestHelper provisioningRequestHelper;
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
        final RoutineManager routineManager = new DefaultRoutineManager(properties,
            this.jobsToSubmit);
        routineManager.startAll();
        this.queueRequestHelper = new QueueRequestHelper(properties);
        this.provisioningRequestHelper = new ProvisioningRequestHelper(properties);
    }

    @Transactional
    public synchronized String submitJob(String queueId, String jdfFilePath, User jobOwner)
        throws CompilerException {
        logger.debug("Adding job of user " + jobOwner.getAlias() + " to buffer.");

        boolean foundQueue = QueueDBManager.getInstance().existsQueueFromUser(queueId, jobOwner);
        if (!foundQueue) {
            throw new QueueNotFoundException("Queue not found [" + queueId + "]");
        }

        final Job job = buildJob(jdfFilePath, jobOwner);
        JobDBManager.getInstance().save(job);
        QueueDBManager.getInstance().addJobToQueue(queueId, job);
        Pair<String, Job> pair = new Pair<>(queueId, job);
        this.jobsToSubmit.offer(pair);

        synchronized (this.jobsToSubmit) {
            this.jobsToSubmit.notifyAll();
        }

        return job.getId();
    }

    private Job buildJob(String jdfFilePath, User owner) throws CompilerException {

        final String ownerAlias = owner.getAlias();
        Job job = new Job(new ArrayList<>(), ownerAlias, owner.getId());

        logger.debug("Building job " + job.getId() + " of user " + owner.getAlias() + " of jdf "
            + jdfFilePath);
        JobSpecification jobSpec = compile(jdfFilePath);
        JDFUtil.removeEmptySpaceFromVariables(jobSpec);
        OAuthToken oAuthToken = owner.getCredentials().getOauthToken();

        return buildJobFromJDFFile(job, jdfFilePath, jobSpec, ownerAlias,
            Objects.requireNonNull(oAuthToken).getAccessToken(), oAuthToken.getVersion());
    }

    @Transactional
    public synchronized User authorizeUser(RequesterCredential requesterCredentials)
        throws UnauthorizedRequestException, UserNotExistException {
        if (Objects.isNull(requesterCredentials.getIguassuToken())) {
            throw new IllegalArgumentException("Iguassu Token missing");
        }

        User authenticatedUser;
        try {
            logger.info("Checking nonce.");

            if (this.nonceList.contains(requesterCredentials.getNonce())) {
                this.nonceList.remove(requesterCredentials.getNonce());
                authenticatedUser = this.authManager.authorize(requesterCredentials);
            } else {
                throw new UnauthorizedRequestException("Invalid nonce for this request");
            }
        } catch (UserNotExistException e)  {
            throw new UserNotExistException();
        }
        return authenticatedUser;
    }

    private JobSpecification compile(String jdfFilePath) throws CompilerException {
        CommonCompiler commonCompiler = new CommonCompiler();
        logger.info("Job " + jdfFilePath + " compilation started at time: " + System.currentTimeMillis());
        commonCompiler.compile(jdfFilePath, CommonCompiler.FileType.JDF);
        logger.info("Job " + jdfFilePath + " compilation ended at time: " + System.currentTimeMillis());
        return (JobSpecification) commonCompiler.getResult().get(0);
    }

    private Job buildJobFromJDFFile(Job job, String jdfFilePath, JobSpecification jobSpec,
        String userAlias, String externalOAuthToken, Long tokenVersion) {
        try {
            this.jobBuilder.createJobFromJDFFile(job, jdfFilePath, jobSpec, userAlias, externalOAuthToken, tokenVersion);
            logger.info("JDF [" + jdfFilePath + "] was built with success at time: [" + System.currentTimeMillis() + "]");
            job.setState(JobState.CREATED);
        } catch (Exception e) {
            logger.error("Failed to build [" + job.getId() + "] : at time: [" + System.currentTimeMillis() + "]", e);
            job.setState(JobState.FAILED);
        }

        return job;
    }

    @Transactional
    public synchronized User authenticateUser(OAuth2Identifiers oAuth2Identifiers,
        String authorizationCode) throws GeneralSecurityException {
        try {
            return this.authManager.authenticate(oAuth2Identifiers, authorizationCode, this.getNonce());
        } catch (Exception gse) {
            throw new GeneralSecurityException(gse.getMessage());
        }
    }

    public synchronized int getNonce() {
        final int nonce = UUID.randomUUID().hashCode();
        this.nonceList.add(nonce);
        return nonce;
    }

    public synchronized OAuthToken refreshToken(OAuthToken oAuthToken)
        throws GeneralSecurityException {
        try {
            return this.authManager.refreshOAuth2Token(oAuthToken);
        } catch (Exception e) {
            throw new GeneralSecurityException(e.getMessage());
        }
    }

    public OAuthToken findUserOAuthTokenByAlias(String userAlias) {
        return this.userDBManager.findUserByAlias(userAlias).getCredentials().getOauthToken();
    }

    public Collection<Job> findAllJobsFromQueueByUserId(String queueId, Long userId) {
        ArrebolQueue queue = QueueDBManager.getInstance().findOne(queueId);
        if (Objects.isNull(queue)) {
            throw new IllegalArgumentException("Queue not found [" + queueId + "]");
        }
        return queue.getJobs().stream()
            .filter(job -> job.getOwnerId().equals(userId)).collect(Collectors.toList());
    }

    public synchronized String removeJob(String jobId, Long userId)
        throws UnauthorizedRequestException {
        Job job = this.jobDBManager.findOne(jobId);
        if (match(job, userId)) {
            job.setState(JobState.REMOVED);
            this.jobDBManager.save(job);
        } else {
            throw new UnauthorizedRequestException(
                "User with id [" + userId + "] does not own this job");
        }

        return job.getId();
    }

    public Job findJobFromQueueById(String queueId, String jobId, User user)
        throws JobNotFoundException, UnauthorizedRequestException {
        Job job;
        ArrebolQueue queue = QueueDBManager.getInstance().findOne(queueId);
        List<Job> jobs = queue.getJobs().stream().filter(job1 -> job1.getId().equals(jobId)).collect(Collectors.toList());
        if (jobs.isEmpty()) {
            logger.error("Could not find job with id [" + jobId + "]");
            throw new JobNotFoundException("Could not find job with id [" + jobId + "]");
        }
        job = jobs.get(0);

        if (!match(job, user.getId())) {
            throw new UnauthorizedRequestException(
                "User with id [" + user.getId() + "] does not own this job");
        }
        return job;
    }

    private boolean match(Job job, Long userId) {
        return job.getOwnerId().equals(userId);
    }

    public synchronized String createQueue(User user, QueueDTORequest queue) {
        logger.info("Creating queue [" + queue.getName() + "] on Arrebol");
        String queueId = null;

        try {
            queueId = queueRequestHelper.createQueue(queue);
            QueueDTO queueCreated = queueRequestHelper.getQueue(queueId);
            QueueDBManager.getInstance().save(queueId, user.getId(), queueCreated.getName());
            return queueId;
        } catch (Exception e) {
            logger.error("Error while creating queue on Arrebol");
            e.printStackTrace();
        }
        return queueId;
    }

    public List<QueueDTO> getQueues(User user) {
        logger.info("Getting queues from user [" + user.getAlias() + "][" + user.getId() + "]");
        List<String> queuesIds = QueueDBManager.getInstance().getQueuesByUser(user).stream()
            .map(ArrebolQueue::getQueueId).collect(Collectors.toList());
        List<QueueDTO> queues = new ArrayList<>();
        for (String id : queuesIds) {
            QueueDTO queue = this.queueRequestHelper.getQueue(id);
            queues.add(queue);
        }
        return queues;
    }

    public Pool addNode(User user, String queueId, ResourceNode node)
        throws Exception {
        ArrebolQueue arrebolQueue = QueueDBManager.getInstance().findOne(queueId);

        verifyUser(arrebolQueue.getOwnerId(), user.getId());

        arrebolQueue.addNode(node.getResourceAddress());

        Thread resourceProvideThread = new ResourceProvideThread("Thread-Resource-Provide-" + user.getAlias() + "-" + queueId,
            queueId, node, provisioningRequestHelper, queueRequestHelper);
        resourceProvideThread.start();

        Pool pool = this.provisioningRequestHelper.getPool(queueId);
        return pool;
    }

    public ResourceDTOResponse getNodes(User user, String queueId) throws UnauthorizedRequestException {
        ArrebolQueue arrebolQueue = QueueDBManager.getInstance().findOne(queueId);

        verifyUser(arrebolQueue.getOwnerId(), user.getId());

        QueueDTO queueDTO = this.queueRequestHelper.getQueue(arrebolQueue.getQueueId());


        return null;
    }

    private void verifyUser(Long queueUserId, Long userId) throws UnauthorizedRequestException {
        if (queueUserId != -1 && !queueUserId.equals(userId)) {
            final String errMsg = "User is not allowed for such operation";
            logger.info(errMsg);
            throw new UnauthorizedRequestException(errMsg);
        }
    }

    public ArrebolQueue getQueue(User user, String queueId) throws UnauthorizedRequestException {
        ArrebolQueue arrebolQueue = QueueDBManager.getInstance().findOne(queueId);
        if (!queueId.equals(DEFAULT_QUEUE_ID)) {
            verifyUser(arrebolQueue.getOwnerId(), user.getId());
        }
        return arrebolQueue;
    }
}
