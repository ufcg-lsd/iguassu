package org.fogbowcloud.app.core.routines;

import java.util.Arrays;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.auth.AuthManager;
import org.fogbowcloud.app.core.constants.ConfProperty;
import org.fogbowcloud.app.core.datastore.JobDataStore;
import org.fogbowcloud.app.core.datastore.OAuthTokenDataStore;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jes.JobExecutionService;
import org.fogbowcloud.app.jes.arrebol.ArrebolJobExecutionService;
import org.fogbowcloud.app.jes.arrebol.ArrebolSynchronizer;
import org.fogbowcloud.app.utils.ManagerTimer;

public class DefaultRoutineManager implements RoutineManager {
    private static final long DEFAULT_INITIAL_DELAY_MS = 3000;
    private static final int DEFAULT_POOL_THREAD_NUMBER = 1;

    private static final Logger logger = Logger.getLogger(DefaultRoutineManager.class);

    private final JobExecutionService jobExecutionSystem;
    private final Properties properties;
    private final OAuthTokenDataStore oAuthTokenDataStore;
    private final JobDataStore jobDataStore;
    private final AuthManager authManager;
    private final Queue<JDFJob> jobsToSubmit;

    public DefaultRoutineManager(
            Properties properties,
            OAuthTokenDataStore oAuthTokenDataStore,
            JobDataStore jobDataStore,
            AuthManager authManager,
            Queue<JDFJob> jobsToSubmit) {

        this.properties = properties;
        this.jobExecutionSystem = new ArrebolJobExecutionService(this.properties);
        this.oAuthTokenDataStore = oAuthTokenDataStore;
        this.jobDataStore = jobDataStore;
        this.authManager = authManager;
        this.jobsToSubmit = jobsToSubmit;
    }

    @Override
    public void startAll() {
        try {
            startSyncJobRoutine();
            startSessionRoutine();
            startJobSubmissionRoutine();
        } catch (Throwable throwable) {
            logger.error(
                    "An error occurred while trying to init system monitors: "
                            + Arrays.toString(throwable.getStackTrace()));
        }
    }

    private void startSyncJobRoutine() {
        logger.debug("----> Starting Sync Job State Routine...");
        final long SYNC_PERIOD =
                Long.parseLong(
                        this.properties.getProperty(
                                ConfProperty.JOB_STATE_MONITOR_PERIOD.getProp()));

        final ManagerTimer syncJobTimer =
                new ManagerTimer(Executors.newScheduledThreadPool(DEFAULT_POOL_THREAD_NUMBER));
        SyncJobStateRoutine syncJobStateRoutine =
                new SyncJobStateRoutine(
                        this.jobDataStore, new ArrebolSynchronizer(this.properties));
        syncJobTimer.scheduleAtFixedRate(
                syncJobStateRoutine, DEFAULT_INITIAL_DELAY_MS, SYNC_PERIOD);
    }

    private void startSessionRoutine() {
        logger.debug("----> Starting Session Verification Routine...");
        final long VERIFICATION_PERIOD =
                Long.parseLong(
                        this.properties.getProperty(ConfProperty.SESSION_MONITOR_PERIOD.getProp()));

        final ManagerTimer verifySessionTimer =
                new ManagerTimer(Executors.newScheduledThreadPool(DEFAULT_POOL_THREAD_NUMBER));
        SessionVerificationRoutine sessionVerificationRoutine =
                new SessionVerificationRoutine(this.oAuthTokenDataStore, this.authManager);
        verifySessionTimer.scheduleAtFixedRate(
                sessionVerificationRoutine, DEFAULT_INITIAL_DELAY_MS, VERIFICATION_PERIOD);
    }

    private void startJobSubmissionRoutine() {
        logger.debug("----> Starting Job Submission Routine...");
        final long SUBMISSION_PERIOD =
                Long.parseLong(
                        this.properties.getProperty(
                                ConfProperty.JOB_SUBMISSION_MONITOR_PERIOD.getProp()));
        final ManagerTimer submissionJobTimer =
                new ManagerTimer(Executors.newScheduledThreadPool(DEFAULT_POOL_THREAD_NUMBER));
        JobSubmissionRoutine jobSubmissionRoutine =
                new JobSubmissionRoutine(
                        this.jobDataStore, this.jobExecutionSystem, this.jobsToSubmit);
        submissionJobTimer.scheduleAtFixedRate(
                jobSubmissionRoutine, DEFAULT_INITIAL_DELAY_MS, SUBMISSION_PERIOD);
    }
}
