package org.fogbowcloud.app.api.http.services;

import java.io.IOException;
import java.util.List;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.IguassuFacade;
import org.fogbowcloud.app.core.authenticator.models.User;
import org.fogbowcloud.app.core.exceptions.InvalidParameterException;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
public class JobService {

    private final Logger logger = Logger.getLogger(JobService.class);

    @Lazy
    @Autowired
    private IguassuFacade iguassuFacade;

    public List<JDFJob> getAllJobs(User user) {
        return this.iguassuFacade.getAllJobs(user.getIdentifier());
    }

    public JDFJob getJobById(String jobId, User user) throws InvalidParameterException {
        JDFJob job = this.iguassuFacade.getJobById(jobId, user.getIdentifier());
        if (job == null) {
            job = this.iguassuFacade.getJobByName(jobId, user.getIdentifier());
            if (job == null) {
                logger.info(
                    "Could not find job with id " + jobId + " for user " + user
                        .getIdentifier());
                throw new InvalidParameterException("Could not find job with id '" + jobId + "'.");
            }
        }

        logger.info("JobID " + jobId + " is of job " + job);

        return job;
    }

    public JDFJob getJobByName(String jobName, String user) {
        return this.iguassuFacade.getJobByName(jobName, user);
    }

    public String stopJob(String jobId, String user) {
        return this.iguassuFacade.stopJob(jobId, user);
    }

    public String submitJob(String jdfFilePath, User user) throws CompilerException, IOException {
        return this.iguassuFacade.submitJob(jdfFilePath, user);
    }
}
