package org.fogbowcloud.app.api.http.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.api.constants.Documentation;
import org.fogbowcloud.app.api.dtos.InvalidRequestDTO;
import org.fogbowcloud.app.api.dtos.JobDTO;
import org.fogbowcloud.app.api.dtos.TaskDTO;
import org.fogbowcloud.app.api.http.services.AuthService;
import org.fogbowcloud.app.api.http.services.FileStorageService;
import org.fogbowcloud.app.api.http.services.JobService;
import org.fogbowcloud.app.core.constants.GeneralConstants;
import org.fogbowcloud.app.core.exceptions.JobNotFoundException;
import org.fogbowcloud.app.core.exceptions.StorageException;
import org.fogbowcloud.app.core.exceptions.UnauthorizedRequestException;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.core.models.task.Task;
import org.fogbowcloud.app.core.models.user.User;
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
@RequestMapping(value = Documentation.Endpoint.QUEUES)
@Api(Documentation.Queue.DESCRIPTION)
public class QueueAPI {

    private final Logger logger = Logger.getLogger(QueueAPI.class);

    @Lazy
    private final FileStorageService storageService;

    @Lazy
    private JobService jobService;

    @Lazy
    private AuthService authService;

    @Autowired
    public QueueAPI(FileStorageService storageService, JobService jobService, AuthService authService) {
        this.storageService = storageService;
        this.jobService = jobService;
        this.authService = authService;
    }

    @PostMapping(value = Documentation.Endpoint.SUBMIT_JOB)
    @ApiOperation(value = Documentation.Queue.SUBMIT_JOB)
    public ResponseEntity<?> submitJob(
            @ApiParam(value = Documentation.Queue.CREATE_REQUEST_PARAM)
            @RequestParam(GeneralConstants.JDF_FILE_PATH) MultipartFile rawJDF,

            @ApiParam(value = Documentation.Queue.QUEUE_ID)
            @PathVariable String queueId,

            @ApiParam(value = Documentation.CommonParameters.USER_CREDENTIALS)
            @RequestHeader(value = GeneralConstants.X_AUTH_USER_CREDENTIALS)
                    String userCredentials) {

        if (Objects.isNull(queueId)) {
            return new ResponseEntity<>(new InvalidRequestDTO("No queued specified"),
                    HttpStatus.BAD_REQUEST);
        }

        // Todo: manages the queue id

        final Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put(GeneralConstants.JDF_FILE_PATH, null);
        fieldMap.put(GeneralConstants.X_AUTH_USER_CREDENTIALS, null);

        this.storageService.store(rawJDF, fieldMap);
        User user;

        try {
            user = this.authService.authorizeUser(userCredentials);
        } catch (UnauthorizedRequestException ure) {
            return new ResponseEntity<>("The authentication failed with error [" + ure.getMessage() + "]",
                    HttpStatus.UNAUTHORIZED);
        }

        final String jdf = fieldMap.get(GeneralConstants.JDF_FILE_PATH);
        if (Objects.isNull(jdf)) {
            logger.info("Could not store new jdf from user " + user.getAlias());
            throw new StorageException("Could not store new job from user " + user.getAlias());
        }

        String jobId;
        final String jdfAbsolutePath = fieldMap.get(GeneralConstants.JDF_FILE_PATH);
        try {
            logger.info("Job description file path's <" + jdfAbsolutePath + ">");
            jobId = this.jobService.submitJob(queueId, jdfAbsolutePath, user);
            logger.info("Job " + jobId + " created at time: " + System.currentTimeMillis());
        } catch (CompilerException ce) {
            logger.error(ce.getMessage(), ce);
            throw new StorageException("Could not compile JDF file", ce);
        } catch (IOException e) {
            logger.error("Could not read JDF file", e);
            throw new StorageException("Could not read JDF file");
        }

        return new ResponseEntity<>(new SimpleJobResponse(jobId), HttpStatus.CREATED);
    }

    @GetMapping(value = Documentation.Endpoint.RETRIEVE_ALL_JOBS)
    @ApiOperation(value = Documentation.Queue.RETRIEVE_ALL_JOBS)
    public ResponseEntity<?> getAllJobs(
            @ApiParam(value = Documentation.Queue.QUEUE_ID)
            @PathVariable String queueId,
            @ApiParam(value = Documentation.CommonParameters.USER_CREDENTIALS)
            @RequestHeader(value = GeneralConstants.X_AUTH_USER_CREDENTIALS) String credentials) {
        logger.info("Request to retrieve all jobs per user received");

        User user;

        if (Objects.isNull(queueId)) {
            return new ResponseEntity<>(new InvalidRequestDTO("No queued specified"),
                    HttpStatus.BAD_REQUEST);
        }

        try {
            user = this.authService.authorizeUser(credentials);
        } catch (UnauthorizedRequestException ure) {
            return new ResponseEntity<>(
                    "Error while trying to authorize [" + ure.getMessage() + "]",
                    HttpStatus.UNAUTHORIZED);
        }

        final Collection<Job> allJobsOfUser = this.jobService.getActiveJobsFromQueueByUser(queueId, user);

        final List<JobDTO> response = new LinkedList<>();

        allJobsOfUser.forEach(job -> response.add(new JobDTO(job)));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping(value = Documentation.Endpoint.RETRIEVE_JOB_BY_ID)
    @ApiOperation(value = Documentation.Queue.RETRIEVE_JOB_BY_ID)
    public ResponseEntity<?> getJobById(
            @ApiParam(value = Documentation.Queue.QUEUE_ID) @PathVariable String queueId,
            @ApiParam(value = Documentation.Queue.JOB_ID) @PathVariable String jobId,
            @ApiParam(value = Documentation.CommonParameters.USER_CREDENTIALS)
            @RequestHeader(value = GeneralConstants.X_AUTH_USER_CREDENTIALS)
                    String userCredentials) {

        if (Objects.isNull(queueId) || Objects.isNull(jobId)) {
            return new ResponseEntity<>(new InvalidRequestDTO("Some fields are missing"),
                    HttpStatus.BAD_REQUEST);
        }

        Job job;
        try {
            job = getJDFJob(queueId, jobId, userCredentials);

        } catch (UnauthorizedRequestException | JobNotFoundException ure) {
            return new ResponseEntity<>(
                    "The authentication failed with error [" + ure.getMessage() + "]",
                    HttpStatus.UNAUTHORIZED);
        }
        logger.info("Retrieving job with id [" + jobId + "]");
        return new ResponseEntity<>(new JobDTO(job), HttpStatus.OK);
    }

    @GetMapping(value = Documentation.Endpoint.RETRIEVE_TASKS_BY_JOB)
    @ApiOperation(value = Documentation.Queue.RETRIEVE_TASKS_BY_JOB)
    public ResponseEntity<?> getJobTasks(
            @ApiParam(value = Documentation.Queue.QUEUE_ID) @PathVariable String queueId,
            @ApiParam(value = Documentation.Queue.JOB_ID) @PathVariable String jobId,
            @ApiParam(value = Documentation.CommonParameters.USER_CREDENTIALS)
            @RequestHeader(value = GeneralConstants.X_AUTH_USER_CREDENTIALS)
                    String userCredentials) {

        if (Objects.isNull(queueId) || Objects.isNull(jobId)) {
            return new ResponseEntity<>(new InvalidRequestDTO("Some fields are missing"),
                    HttpStatus.BAD_REQUEST);
        }

        Job job;
        try {
            job = getJDFJob(queueId, jobId, userCredentials);

        } catch (UnauthorizedRequestException | JobNotFoundException ure) {
            return new ResponseEntity<>(
                    "The authentication failed with error [" + ure.getMessage() + "]",
                    HttpStatus.UNAUTHORIZED);
        }
        logger.info("Retrieving tasks from job with id [" + jobId + "]");
        Collection<TaskDTO> response = generateTaskList(job.getTasks());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping(value = Documentation.Endpoint.DELETE_JOB_BY_ID)
    @ApiOperation(value = Documentation.Queue.DELETE_JOB_BY_ID)
    public ResponseEntity<?> stopJob(
            @ApiParam(value = Documentation.Queue.QUEUE_ID) @PathVariable String queueId,
            @ApiParam(value = Documentation.Queue.JOB_ID) @PathVariable String jobId,
            @ApiParam(value = Documentation.CommonParameters.USER_CREDENTIALS)
            @RequestHeader(value = GeneralConstants.X_AUTH_USER_CREDENTIALS)
                    String userCredentials) {

        if (Objects.isNull(queueId) || Objects.isNull(jobId)) {
            return new ResponseEntity<>(new InvalidRequestDTO("Some fields are missing"),
                    HttpStatus.BAD_REQUEST);
        }

        logger.info("Deleting job with Id " + jobId + ".");

        User user;

        try {
            user = this.authService.authorizeUser(userCredentials);
        } catch (UnauthorizedRequestException ure) {
            return new ResponseEntity<>(
                    "The authentication failed with error [" + ure.getMessage() + "]",
                    HttpStatus.UNAUTHORIZED);
        }

        String removedJob;
        try {
            removedJob = this.jobService.removeJob(jobId, user.getId());
        } catch (UnauthorizedRequestException e) {
            logger.error(e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(new SimpleJobResponse(removedJob), HttpStatus.ACCEPTED);
    }

    private Collection<TaskDTO> generateTaskList(Collection<Task> tasks) {
        final Collection<TaskDTO> l = new ArrayList<>();
        for (Task t : tasks) {
            l.add(new TaskDTO(t));
        }
        return l;
    }

    private Job getJDFJob(String queueId, String jobId, String userCredentials)
            throws UnauthorizedRequestException, JobNotFoundException {
        final User user = this.authService.authorizeUser(userCredentials);

        return this.jobService.getJobFromQueueById(queueId, jobId, user);
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
