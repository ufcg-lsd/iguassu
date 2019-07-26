package org.fogbowcloud.app.core.monitor;

import java.util.Arrays;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.auth.AuthManager;
import org.fogbowcloud.app.core.constants.ConfProperties;
import org.fogbowcloud.app.core.datastore.JobDataStore;
import org.fogbowcloud.app.core.datastore.OAuthTokenDataStore;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jes.JobExecutionService;
import org.fogbowcloud.app.jes.arrebol.ArrebolJobExecutionService;
import org.fogbowcloud.app.jes.arrebol.ArrebolSynchronizer;
import org.fogbowcloud.app.utils.ManagerTimer;

public class DefaultMonitorManager implements MonitorManager {
	private static final long DEFAULT_INITIAL_DELAY_MS = 3000;
	private static final Logger logger = Logger.getLogger(DefaultMonitorManager.class);

	private static ManagerTimer executionMonitorTimer =
		new ManagerTimer(Executors.newScheduledThreadPool(1));
	private static ManagerTimer sessionMonitorTimer =
		new ManagerTimer(Executors.newScheduledThreadPool(1));
	private static ManagerTimer submissionMonitorTimer =
		new ManagerTimer(Executors.newScheduledThreadPool(1));

	private final JobExecutionService jobExecutionSystem;
	private final Properties properties;
	private final OAuthTokenDataStore oAuthTokenDataStore;
	private final JobDataStore jobDataStore;
	private final AuthManager authManager;
	private final Queue<JDFJob> jobsToSubmit;

	public DefaultMonitorManager(
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
	public void start() {
		try {
			initMonitors();
		} catch (Throwable throwable) {
			logger.error(
				"An error occurred while trying to init system monitors: "
					+ Arrays.toString(throwable.getStackTrace()));
		}
	}

	private void initMonitors() {
		initJobStateMonitor();
		initSessionMonitor();
		initJobSubmissionMonitor();
	}

	private void initJobStateMonitor() {
		final long JOB_MONITOR_EXECUTION_PERIOD =
			Long.valueOf(this.properties.getProperty(ConfProperties.JOB_STATE_MONITOR_PERIOD));

		JobStateMonitor jobStateMonitor =
			new JobStateMonitor(this.jobDataStore, new ArrebolSynchronizer(this.properties));
		executionMonitorTimer.scheduleAtFixedRate(
			jobStateMonitor, DEFAULT_INITIAL_DELAY_MS, JOB_MONITOR_EXECUTION_PERIOD);
	}

	private void initSessionMonitor() {
		final long SESSION_MONITOR_EXECUTION_PERIOD =
			Long.valueOf(this.properties.getProperty(ConfProperties.SESSION_MONITOR_PERIOD));

		SessionMonitor sessionMonitor = new SessionMonitor(this.oAuthTokenDataStore, this.authManager);
		sessionMonitorTimer.scheduleAtFixedRate(
			sessionMonitor, DEFAULT_INITIAL_DELAY_MS, SESSION_MONITOR_EXECUTION_PERIOD);
	}

	private void initJobSubmissionMonitor() {
		final long SUBMISSION_MONITOR_EXECUTION_PERIOD =
			Long.valueOf(this.properties.getProperty(ConfProperties.JOB_SUBMISSION_MONITOR_PERIOD));

		JobSubmissionMonitor jobSubmissionMonitor =
			new JobSubmissionMonitor(this.jobDataStore, this.jobExecutionSystem, this.jobsToSubmit);
		submissionMonitorTimer.scheduleAtFixedRate(
			jobSubmissionMonitor, DEFAULT_INITIAL_DELAY_MS, SUBMISSION_MONITOR_EXECUTION_PERIOD);
	}
}
