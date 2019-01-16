package org.fogbowcloud.app.api.http.services;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.IguassuController;
import org.fogbowcloud.app.NameAlreadyInUseException;
import org.fogbowcloud.app.exception.InvalidParameterException;
import org.fogbowcloud.app.exception.ResourceException;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.app.model.JDFJob;
import org.fogbowcloud.app.model.Status;
import org.fogbowcloud.app.model.User;
import org.fogbowcloud.blowout.core.exception.BlowoutException;
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
    IguassuController iguassuController;

    private final Logger LOGGER = Logger.getLogger(JobService.class);

    public List<JDFJob> getAllJobs(User owner) {
        return this.iguassuController.getAllJobs(owner.getUser());
    }

    public JDFJob getJobById(String jobId, User owner) throws InvalidParameterException {
        JDFJob job = this.iguassuController.getJobById(jobId, owner.getUser());
        if (job == null) {
            job = this.iguassuController.getJobByName(jobId, owner.getUser());
            if (job == null) {
                LOGGER.info("Could not find job with id " + jobId + " for user " + owner.getUsername());
                throw new InvalidParameterException("Could not find job with id '" + jobId + "'.");
            }
        }

        LOGGER.info("JobID " + jobId + " is of job " + job);

        return job;
    }

    public JDFJob getJobByName(String jobName, String owner) {
        return this.iguassuController.getJobByName(jobName, owner);
    }

    public String stopJob(String jobId, String owner) {
        return this.iguassuController.stopJob(jobId, owner);
    }

    public String addJob(String jdfFilePath, User owner)
            throws CompilerException, NameAlreadyInUseException, BlowoutException, IOException {
        return this.iguassuController.addJob(jdfFilePath, owner);
    }

    public User authenticateUser(String credentials) {
        User owner;
        try {
            owner = this.iguassuController.authUser(credentials);
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
        }
        if (owner == null) {
            LOGGER.error("Authentication failed. Wrong username/password.");
            throw new ResourceException(
                    Status.CLIENT_ERROR_UNAUTHORIZED,
                    "Incorrect username/password."
            );
        }
        return owner;
    }

}
