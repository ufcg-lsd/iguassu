package org.fogbowcloud.app.jes.arrebol;

import java.util.Properties;
import javax.print.attribute.standard.JobState;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.dto.ArrebolJobDTO;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jes.JobExecutionSystem;
import org.fogbowcloud.app.jes.exceptions.GetJobException;
import org.fogbowcloud.app.jes.exceptions.SubmitJobException;

public class ArrebolJobExecutionSystem implements JobExecutionSystem {

  private static final Logger LOGGER = Logger.getLogger(ArrebolJobExecutionSystem.class);

  private final ArrebolRequestsHelper requestsHelper;

  public ArrebolJobExecutionSystem(Properties properties) {
    this.requestsHelper = new ArrebolRequestsHelper(properties);
  }

  @Override
  public String execute(JDFJob job) {
    LOGGER.info("Execution for the Job with id :[" + job.getId() + "] was started");

    String jobIdArrebol = null;
    try {
      jobIdArrebol = this.requestsHelper.submitJobToExecution(job);
    } catch (Exception | SubmitJobException sje) {
      LOGGER.error("Error while submitting job with id: [" + job.getId() + "]",
          sje);
    }

    return jobIdArrebol;

  }

  // TODO this return JDF
  @Override
  public ArrebolJobDTO getJob(String jobId) {
    try {
      return requestsHelper.getJob(jobId);
    } catch (GetJobException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public JobState jobState(String executionId) {
    return null;
  }


  @Override
  public void stop(String executionId) {

  }
}
