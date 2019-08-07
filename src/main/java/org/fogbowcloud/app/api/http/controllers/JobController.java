package org.fogbowcloud.app.api.http.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.api.constants.Documentation;
import org.fogbowcloud.app.api.exceptions.StorageException;
import org.fogbowcloud.app.api.http.services.AuthService;
import org.fogbowcloud.app.api.http.services.FileStorageService;
import org.fogbowcloud.app.api.http.services.JobService;
import org.fogbowcloud.app.core.constants.GeneralConstants;
import org.fogbowcloud.app.core.dto.JobResponseDTO;
import org.fogbowcloud.app.core.dto.TaskDTO;
import org.fogbowcloud.app.core.exceptions.InvalidParameterException;
import org.fogbowcloud.app.core.exceptions.UnauthorizedRequestException;
import org.fogbowcloud.app.core.models.auth.User;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.core.models.task.Task;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping(value = Documentation.Endpoint.JOB)
@Api(Documentation.Job.DESCRIPTION)
public class JobController {

    private final Logger logger = Logger.getLogger(JobController.class);

    @Lazy private final FileStorageService storageService;

    @Lazy private JobService jobService;

    @Lazy private AuthService authService;

    @Autowired
    public JobController(
            FileStorageService storageService, JobService jobService, AuthService authService) {
        this.storageService = storageService;
        this.jobService = jobService;
        this.authService = authService;
    }

    @GetMapping(value = Documentation.Endpoint.STATUS)
    @ApiOperation(value = Documentation.Job.GET_ALL_OPERATION)
    public ResponseEntity<?> getAllJobs(
            @ApiParam(value = Documentation.CommonParameters.USER_CREDENTIALS)
                    @RequestHeader(value = GeneralConstants.X_AUTH_USER_CREDENTIALS)
                    String userCredentials) {
        User user;

        try {
            user = this.authService.authorizeUser(userCredentials);

        } catch (UnauthorizedRequestException ure) {
            return new ResponseEntity<>(
                    "The authentication failed with error [" + ure.getMessage() + "]",
                    HttpStatus.UNAUTHORIZED);
        }

        final Collection<Job> allJobsOfUser = this.jobService.getJobsByUser(user);;
//
        logger.debug("Retrieving all jobs of user [" + user.getName() + "]");

        final List<JobResponseDTO> jobsResponse = new LinkedList<>();

        allJobsOfUser.forEach(job -> jobsResponse.add(new JobResponseDTO(job)));

        return new ResponseEntity<>(jobsResponse, HttpStatus.OK);
    }

    @GetMapping(value = Documentation.Endpoint.JOB_ID)
    @ApiOperation(value = Documentation.Job.GET_BY_ID_OPERATION)
    public ResponseEntity<?> getJobById(
            @ApiParam(value = Documentation.Job.ID) @PathVariable String jobId,
            @ApiParam(value = Documentation.CommonParameters.USER_CREDENTIALS)
                    @RequestHeader(value = GeneralConstants.X_AUTH_USER_CREDENTIALS)
                    String userCredentials)
            throws InvalidParameterException {

        Job job;
        try {
            job = getJDFJob(jobId, userCredentials);

        } catch (UnauthorizedRequestException ure) {
            return new ResponseEntity<>(
                    "The authentication failed with error [" + ure.getMessage() + "]",
                    HttpStatus.UNAUTHORIZED);
        }
        logger.info("Retrieving job with id [" + jobId + "].");
        return new ResponseEntity<>(new JobResponseDTO(job), HttpStatus.OK);
    }

    @GetMapping(
            value =
                    Documentation.Endpoint.JOB_ID
                            + Documentation.Endpoint.TASK
                            + Documentation.Endpoint.STATUS)
    @ApiOperation(value = Documentation.Job.GET_TASKS_OPERATION)
    public ResponseEntity<?> getJobTasks(
            @ApiParam(value = Documentation.Job.ID) @PathVariable String jobId,
            @ApiParam(value = Documentation.CommonParameters.USER_CREDENTIALS)
                    @RequestHeader(value = GeneralConstants.X_AUTH_USER_CREDENTIALS)
                    String userCredentials)
            throws InvalidParameterException {
        Job job;
        try {
            job = getJDFJob(jobId, userCredentials);

        } catch (UnauthorizedRequestException ure) {
            return new ResponseEntity<>(
                    "The authentication failed with error [" + ure.getMessage() + "]",
                    HttpStatus.UNAUTHORIZED);
        }
        logger.info("Retrieving tasks from job with id [" + jobId + "].");
        Collection<TaskDTO> taskDTOS = toTasksDTOList(job.getTasksAsList());
        return new ResponseEntity<>(taskDTOS, HttpStatus.OK);
    }

    @PostMapping
    @ApiOperation(value = Documentation.Job.CREATE_OPERATION)
    public ResponseEntity<String> submitJob(
            @ApiParam(value = Documentation.Job.CREATE_REQUEST_PARAM)
                    @RequestParam(GeneralConstants.JDF_FILE_PATH)
                    MultipartFile file,
            @ApiParam(value = Documentation.CommonParameters.USER_CREDENTIALS)
                    @RequestHeader(value = GeneralConstants.X_AUTH_USER_CREDENTIALS)
                    String userCredentials) {

        logger.info("Saving new Job.");
        logger.info(file.toString());

        final Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put(GeneralConstants.JDF_FILE_PATH, null);
        fieldMap.put(GeneralConstants.X_AUTH_USER_CREDENTIALS, null);

        this.storageService.store(file, fieldMap);
        User user;

        try {
            user = this.authService.authorizeUser(userCredentials);
        } catch (UnauthorizedRequestException ure) {
            return new ResponseEntity<>(
                    "The authentication failed with error [" + ure.getMessage() + "]",
                    HttpStatus.UNAUTHORIZED);
        }

        final String jdf = fieldMap.get(GeneralConstants.JDF_FILE_PATH);
        if (Objects.isNull(jdf)) {
            logger.info("Could not store  new job from user " + user.getName());
            throw new StorageException("Could not store new job from user " + user.getName());
        }

        String jobId;
        final String jdfAbsolutePath = fieldMap.get(GeneralConstants.JDF_FILE_PATH);
        try {
            logger.info("jdfpath <" + jdfAbsolutePath + ">");
            jobId = this.jobService.submitJob(jdfAbsolutePath, user);
            logger.info("Job " + jobId + " created at time: " + System.currentTimeMillis());
        } catch (CompilerException ce) {
            logger.error(ce.getMessage(), ce);
            throw new StorageException("Could not compile JDF file.", ce);
        } catch (IOException e) {
            logger.error("Could not read JDF file.", e);
            throw new StorageException("Could not read JDF file.");
        }
        return new ResponseEntity<>(jobId, HttpStatus.CREATED);
    }

    @DeleteMapping(value = Documentation.Endpoint.JOB_ID)
    @ApiOperation(value = Documentation.Job.DELETE_OPERATION)
    public ResponseEntity<?> stopJob(
            @ApiParam(value = Documentation.Job.ID) @PathVariable String jobId,
            @ApiParam(value = Documentation.CommonParameters.USER_CREDENTIALS)
                    @RequestHeader(value = GeneralConstants.X_AUTH_USER_CREDENTIALS)
                    String userCredentials)
            throws InvalidParameterException {
        logger.info("Deleting job with Id " + jobId + ".");

        User user;

        try {
            user = this.authService.authorizeUser(userCredentials);
        } catch (UnauthorizedRequestException ure) {
            return new ResponseEntity<>(
                    "The authentication failed with error [" + ure.getMessage() + "]",
                    HttpStatus.UNAUTHORIZED);
        }

        final String stoppedJobId = "";
//        this.jobService.stopJob(jobId, user.getIdentifier());

        if (Objects.isNull(stoppedJobId)) {
            logger.info(
                    "Could not find job with id "
                            + jobId
                            + " for user ["
                            + user.getName()
                            + "].");
            throw new InvalidParameterException("Could not find job with id [" + jobId + "].");
        }

        return new ResponseEntity<>(new SimpleJobResponse(stoppedJobId), HttpStatus.ACCEPTED);
    }

    private Collection<TaskDTO> toTasksDTOList(Collection<Task> tasks) {
        final Collection<TaskDTO> l = new ArrayList<>();
        for (Task t : tasks) {
            l.add(new TaskDTO(t));
        }
        return l;
    }

    private Job getJDFJob(String jobId, String userCredentials)
            throws InvalidParameterException, UnauthorizedRequestException {
        final User user = this.authService.authorizeUser(userCredentials);

        return this.jobService.getJobById(jobId, user);
    }

    static class SimpleJobResponse {

        private String id;

        SimpleJobResponse(String id) {
            this.id = id;
        }

        public String getId() {
            return this.id;
        }
    }
}
