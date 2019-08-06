package org.fogbowcloud.app.api.http.services;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.IguassuFacade;
import org.fogbowcloud.app.core.models.auth.User;
import org.fogbowcloud.app.core.exceptions.InvalidParameterException;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Lazy
@Service
public class JobService {

    private final Logger logger = Logger.getLogger(JobService.class);

    @Lazy @Autowired private IguassuFacade iguassuFacade;

    public List<Job> getAllJobs(User user) {
        return this.iguassuFacade.getAllJobs(user.getIdentifier());
    }

    public Job getJobById(String jobId, User user) throws InvalidParameterException {
        Job job = this.iguassuFacade.getJobById(jobId, user.getIdentifier());
        if (Objects.isNull(job)) {
            logger.info(
                    "Could not find job with id " + jobId + " for user " + user.getIdentifier());
            throw new InvalidParameterException("Could not find job with id '" + jobId + "'.");
        }
        return job;
    }

    public String stopJob(String jobId, String user) {
        return this.iguassuFacade.stopJob(jobId, user);
    }

    public String submitJob(String jdfFilePath, User user) throws CompilerException, IOException {
        return this.iguassuFacade.submitJob(jdfFilePath, user);
    }
}
