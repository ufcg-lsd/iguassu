package org.fogbowcloud.app.api.http.services;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.ApplicationFacade;
import org.fogbowcloud.app.core.exceptions.InvalidParameterException;
import org.fogbowcloud.app.core.models.user.User;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

@Lazy
@Service
public class JobService {

    private final Logger logger = Logger.getLogger(JobService.class);

    private ApplicationFacade applicationFacade = ApplicationFacade.getInstance();

    public Collection<Job> getJobsByUser(User user) {
        return this.applicationFacade.findJobsByUserAlias(user.getAlias());
    }

    public Job getJobById(String jobId, User user) throws InvalidParameterException {
        Job job = null;

        if (Objects.isNull(job)) {
            logger.info(
                    "Could not find job with id " + jobId + " for user " + user.getAlias());
            throw new InvalidParameterException("Could not find job with id '" + jobId + "'.");
        }
        return job;
    }
    public String removeJob(String jobId, String user) {
        return this.applicationFacade.removeJob(jobId, user);
    }

    public long submitJob(String jdfFilePath, User user) throws CompilerException, IOException {
        return this.applicationFacade.submitJob(jdfFilePath, user);
    }
}
