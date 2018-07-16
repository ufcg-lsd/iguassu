package org.fogbowcloud.app.api.http.services;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.ArrebolController;
import org.fogbowcloud.app.NameAlreadyInUseException;
import org.fogbowcloud.app.exception.InvalidParameterException;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.app.model.JDFJob;
import org.fogbowcloud.app.model.LDAPUser;
import org.fogbowcloud.app.model.User;
import org.fogbowcloud.blowout.core.exception.BlowoutException;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Lazy
@Component
public class JobService {

    @Lazy
    @Autowired
    ArrebolController arrebolController;

    private final Logger LOGGER = Logger.getLogger(JobService.class);

    public List<JDFJob> getAllJobs(User owner) {
        return this.arrebolController.getAllJobs(owner.getUser());
    }

    public JDFJob getJobById(String jobId, User owner) throws InvalidParameterException {
        JDFJob job = this.arrebolController.getJobById(jobId, owner.getUser());
        if (job == null) {
            job = this.arrebolController.getJobByName(jobId, owner.getUser());
            if (job == null) {
                LOGGER.debug("Could not find job with id " + jobId + " for user " + owner.getUsername());
                throw new InvalidParameterException("Could not find job with id '" + jobId + "'.");
            }
        }

        LOGGER.debug("JobID " + jobId + " is of job " + job);

        return job;
    }

    public JDFJob getJobByName(String jobName, String owner) {
        return this.arrebolController.getJobByName(jobName, owner);
    }

    public String stopJob(String jobId, String owner) {
        return this.arrebolController.stopJob(jobId, owner);
    }

    public String addJob(String jdfFilePath, User owner)
            throws CompilerException, NameAlreadyInUseException, BlowoutException, IOException {
        return this.arrebolController.addJob(jdfFilePath, owner);
    }

    public User authenticateUser(String credentials) {
        User owner;
        return new LDAPUser("arrebolservice", "arrebolservice"); // TODO
//        try {
//            owner = this.arrebolController.authUser(credentials);
//        } catch (GeneralSecurityException e) {
//            LOGGER.error("Error trying to authenticate", e);
//            throw new ResourceException(
//                    Status.CLIENT_ERROR_UNAUTHORIZED,
//                    "There was an error trying to authenticate.\nTry again later."
//            );
//        } catch (IOException e) {
//            LOGGER.error("Error trying to authenticate", e);
//            throw new ResourceException(
//                    Status.CLIENT_ERROR_BAD_REQUEST,
//                    "Failed to read request header."
//            );
//        }
//        if (owner == null) {
//            LOGGER.error("Authentication failed. Wrong username/password.");
//            throw new ResourceException(
//                    Status.CLIENT_ERROR_UNAUTHORIZED,
//                    "Incorrect username/password."
//            );
//        }
//        return owner;
    }

}
