package org.fogbowcloud.app.api.http.services;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.ApplicationFacade;
import org.fogbowcloud.app.core.exceptions.InvalidParameterException;
import org.fogbowcloud.app.core.exceptions.JobNotFoundException;
import org.fogbowcloud.app.core.exceptions.UnauthorizedRequestException;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.core.models.user.User;
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
        return this.applicationFacade.findAllJobsByUserId(user.getId());
    }

    public Job getJobById(Long jobId, User user) throws JobNotFoundException, UnauthorizedRequestException {
        return this.applicationFacade.findJobById(jobId, user);
    }

    public Long removeJob(Long jobId, Long userId) throws UnauthorizedRequestException {
        return this.applicationFacade.removeJob(jobId, userId);
    }

    public Long submitJob(String jdfFilePath, User user) throws CompilerException, IOException {
        return this.applicationFacade.submitJob(jdfFilePath, user);
    }
}
