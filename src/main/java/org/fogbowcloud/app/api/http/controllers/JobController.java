package org.fogbowcloud.app.api.http.controllers;

import org.fogbowcloud.app.NameAlreadyInUseException;
import org.fogbowcloud.app.api.http.exceptions.StorageException;
import org.fogbowcloud.app.api.http.services.FileSystemStorageService;
import org.fogbowcloud.app.api.http.services.JobService;
import org.fogbowcloud.app.exception.ArrebolException;
import org.fogbowcloud.app.exception.InvalidParameterException;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.app.model.JDFJob;
import org.fogbowcloud.app.model.User;
import org.fogbowcloud.app.utils.ArrebolPropertiesConstants;
import org.fogbowcloud.blowout.core.exception.BlowoutException;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping(value = JobController.JOB_ENDPOINT)
public class JobController {

    protected static final String JOB_ENDPOINT = "job";
    private final String JOB_PATH = "/{jobId}";
    public static final String JDF_FILE_PATH = "jdffilepath";

    private final Logger LOGGER = LoggerFactory.getLogger(JobController.class);

    @Lazy
    JobService jobService;

    private final FileSystemStorageService storageService;

    @Autowired
    public JobController(FileSystemStorageService storageService, JobService jobService) {
        this.storageService = storageService;
        this.jobService = jobService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<JDFJob>> getAllJobs(
            @RequestHeader(value=ArrebolPropertiesConstants.X_CREDENTIALS) String credentials) {
        LOGGER.info("Retrieving all jobs.");

        User owner = this.jobService.authenticateUser(credentials);
        List<JDFJob> list = this.jobService.getAllJobs(owner);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @RequestMapping(value = JOB_PATH, method = RequestMethod.GET)
    public ResponseEntity<JDFJob> getJobById(@PathVariable String jobId,
        @RequestHeader(value=ArrebolPropertiesConstants.X_CREDENTIALS) String credentials) throws InvalidParameterException {
        LOGGER.info("Retrieving job with id " + jobId + "].");

        User owner = this.jobService.authenticateUser(credentials);
        JDFJob job = this.jobService.getJobById(jobId, owner);

        if (job == null) {
            job = this.jobService.getJobByName(jobId, owner.getUser());
            if (job == null) {
                LOGGER.debug("Could not find job with id " + jobId + " for user " + owner.getUsername());
                throw new InvalidParameterException("Could not find job with id '" + jobId + "'.");
            }
        }

        return new ResponseEntity<>(job, HttpStatus.OK);
    }

}


