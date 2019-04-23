package org.fogbowcloud.app.api.http.services;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.IguassuFacade;
import org.fogbowcloud.app.core.exceptions.InvalidParameterException;
import org.fogbowcloud.app.core.exceptions.ResourceException;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.core.authenticator.models.Status;
import org.fogbowcloud.app.core.authenticator.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Lazy
@Component
public class JobService {

    @Lazy
    @Autowired
    IguassuFacade iguassuFacade;

    private final Logger LOGGER = Logger.getLogger(JobService.class);

    public List<JDFJob> getAllJobs(User owner) {
        return this.iguassuFacade.getAllJobs(owner.getUser());
    }

    public JDFJob getJobById(String jobId, User owner) throws InvalidParameterException {
        JDFJob job = this.iguassuFacade.getJobById(jobId, owner.getUser());
        if (job == null) {
            job = this.iguassuFacade.getJobByName(jobId, owner.getUser());
            if (job == null) {
                LOGGER.info("Could not find job with id " + jobId + " for user " + owner.getUsername());
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

    public String addJob(String jdfFilePath, User owner)
            throws CompilerException, IOException {
        return this.iguassuFacade.addJob(jdfFilePath, owner);
    }

    public User authenticateUser(String credentials) {
        User owner;
        try {
            owner = this.iguassuFacade.authUser(credentials);
            LOGGER.info("Retrieving user " + owner.getUsername());
        } catch (GeneralSecurityException e) {
            LOGGER.error("Error trying to authenticate", e);
            throw new ResourceException(
                    Status.CLIENT_ERROR_UNAUTHORIZED,
                    "There was an error trying to authenticate.\nTry again later."
            );
        } catch (IOException e) {
            LOGGER.error("Error trying to authenticate", e);
            throw new ResourceException(
                    Status.CLIENT_ERROR_BAD_REQUEST,
                    "Failed to read request header."
            );
        } catch (NullPointerException e) {
            LOGGER.error("Authentication failed. Wrong username/password.");
            throw new ResourceException(
                    Status.CLIENT_ERROR_UNAUTHORIZED,
                    "Incorrect username/password."
            );
        }
        return owner;
    }

}
