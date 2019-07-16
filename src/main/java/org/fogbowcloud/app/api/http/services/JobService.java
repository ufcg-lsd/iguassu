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

    private final Logger LOGGER = Logger.getLogger(JobService.class);
    @Lazy
    @Autowired
    private IguassuFacade iguassuFacade;

    public List<JDFJob> getAllJobs(User owner) {
        return this.iguassuFacade.getAllJobs(owner.getUserIdentification());
    }

    public JDFJob getJobById(String jobId, User owner) throws InvalidParameterException {
        JDFJob job = this.iguassuFacade.getJobById(jobId, owner.getUserIdentification());
        if (job == null) {
            job = this.iguassuFacade.getJobByName(jobId, owner.getUserIdentification());
            if (job == null) {
                LOGGER.info(
                    "Could not find job with id " + jobId + " for user " + owner
                        .getUserIdentification());
                throw new InvalidParameterException("Could not find job with id '" + jobId + "'.");
            }
        }

        LOGGER.info("JobID " + jobId + " is of job " + job);

        return job;
    }

    public JDFJob getJobByName(String jobName, String owner) {
        return this.iguassuFacade.getJobByName(jobName, owner);
    }

    public String stopJob(String jobId, String owner) {
        return this.iguassuFacade.stopJob(jobId, owner);
    }

    public String submitJob(String jdfFilePath, User owner) throws CompilerException, IOException {
        return this.iguassuFacade.submitJob(jdfFilePath, owner);
    }
}
