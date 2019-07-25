package org.fogbowcloud.app.api.http.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.api.constants.ApiDocumentation.*;
import org.fogbowcloud.app.api.exceptions.StorageException;
import org.fogbowcloud.app.api.http.services.AuthService;
import org.fogbowcloud.app.api.http.services.FileStorageService;
import org.fogbowcloud.app.api.http.services.JobService;
import org.fogbowcloud.app.core.authenticator.models.User;
import org.fogbowcloud.app.core.constants.ConfProperties;
import org.fogbowcloud.app.core.dto.JobResponseDTO;
import org.fogbowcloud.app.core.dto.TaskDTO;
import org.fogbowcloud.app.core.exceptions.InvalidParameterException;
import org.fogbowcloud.app.core.exceptions.UnauthorizedRequestException;
import org.fogbowcloud.app.core.task.Task;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = Endpoint.JOB)
@Api(Job.API_DESCRIPTION)
public class JobController {

    private final Logger logger = Logger.getLogger(JobController.class);

    @Lazy
    private final FileStorageService storageService;

    @Lazy
    private JobService jobService;

    @Lazy
    private AuthService authService;

    @Autowired
    public JobController(FileStorageService storageService, JobService jobService,
                         AuthService authService) {
        this.storageService = storageService;
        this.jobService = jobService;
        this.authService = authService;
    }

    @GetMapping(value = Endpoint.STATUS)
    @ApiOperation(value = Job.GET_ALL_OPERATION)
    public ResponseEntity<?> getAllJobs(
        @ApiParam(value = CommonParameters.USER_CREDENTIALS)
        @RequestHeader(value = ConfProperties.X_AUTH_USER_CREDENTIALS) String userCredentials) {
        User user;

        try {
            user = this.authService.authorizeUser(userCredentials);

        } catch (UnauthorizedRequestException ure) {
            return new ResponseEntity<>(
                "The authentication failed with error [" + ure.getMessage() +
                    "]", HttpStatus.UNAUTHORIZED);
        }

        List<JDFJob> allJobs = this.jobService.getAllJobs(user);
        logger.debug("Retrieving all jobs of user [ " + user.getUserIdentification() + " ]");

        List<JobResponseDTO> jobs = new LinkedList<>();
        for (JDFJob job : allJobs) {
            jobs.add(new JobResponseDTO(job));
        }
        return new ResponseEntity<>(jobs, HttpStatus.OK);
    }

    @GetMapping(value = Endpoint.JOB_ID)
    @ApiOperation(value = Job.GET_BY_ID_OPERATION)
    public ResponseEntity<?> getJobById(
        @ApiParam(value = Job.ID)
        @PathVariable String jobId,
        @ApiParam(value = CommonParameters.USER_CREDENTIALS)
        @RequestHeader(value = ConfProperties.X_AUTH_USER_CREDENTIALS) String userCredentials)
        throws InvalidParameterException {

        JDFJob job;
        try {
            job = getJDFJob(jobId, userCredentials);

        } catch (UnauthorizedRequestException ure) {
            return new ResponseEntity<>(
                "The authentication failed with error [" + ure.getMessage() +
                    "]", HttpStatus.UNAUTHORIZED);
        }
        logger.info("Retrieving job with id [" + jobId + "].");
        return new ResponseEntity<>(new JobResponseDTO(job), HttpStatus.OK);
    }

    @GetMapping(value = Endpoint.JOB_ID + Endpoint.TASK + Endpoint.STATUS)
    @ApiOperation(value = Job.GET_TASKS_OPERATION)
    public ResponseEntity<?> getJobTasks(
        @ApiParam(value = Job.ID)
        @PathVariable String jobId,
        @ApiParam(value = CommonParameters.USER_CREDENTIALS)
        @RequestHeader(value = ConfProperties.X_AUTH_USER_CREDENTIALS) String userCredentials)
        throws InvalidParameterException {
        JDFJob job;
        try {
            job = getJDFJob(jobId, userCredentials);

        } catch (UnauthorizedRequestException ure) {
            return new ResponseEntity<>(
                "The authentication failed with error [" + ure.getMessage() +
                    "]", HttpStatus.UNAUTHORIZED);
        }
        logger.info("Retrieving tasks from job with id [" + jobId + "].");
        List<TaskDTO> taskDTOS = toTasksDTOList(job.getTasks());
        return new ResponseEntity<>(taskDTOS, HttpStatus.OK);
    }

    @PostMapping
    @ApiOperation(value = Job.CREATE_OPERATION)
    public ResponseEntity<String> submitJob(
        @ApiParam(value = Job.CREATE_REQUEST_PARAM)
        @RequestParam(ConfProperties.JDF_FILE_PATH) MultipartFile file,
        @ApiParam(value = CommonParameters.USER_CREDENTIALS)
        @RequestHeader(value = ConfProperties.X_AUTH_USER_CREDENTIALS) String userCredentials) {

        logger.info("Saving new Job.");
        logger.info(file.toString());

        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put(ConfProperties.JDF_FILE_PATH, null);
        fieldMap.put(ConfProperties.X_AUTH_USER_CREDENTIALS, null);

        this.storageService.store(file, fieldMap);
        User user;

        try {
            user = this.authService.authorizeUser(userCredentials);
        } catch (UnauthorizedRequestException ure) {
            return new ResponseEntity<>(
                "The authentication failed with error [" + ure.getMessage() +
                    "]", HttpStatus.UNAUTHORIZED);
        }

        final String jdf = fieldMap.get(ConfProperties.JDF_FILE_PATH);
        if (Objects.isNull(jdf)) {
            logger.info("Could not store  new job from user " + user.getUserIdentification());
            throw new StorageException(
                "Could not store new job from user " + user.getUserIdentification());
        }

        String jobId;
        final String jdfAbsolutePath = fieldMap.get(ConfProperties.JDF_FILE_PATH);
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

    @DeleteMapping(value = Endpoint.JOB_ID)
    @ApiOperation(value = Job.DELETE_OPERATION)
    public ResponseEntity<?> stopJob(
        @ApiParam(value = Job.ID)
        @PathVariable String jobId,
        @ApiParam(value = CommonParameters.USER_CREDENTIALS)
        @RequestHeader(value = ConfProperties.X_AUTH_USER_CREDENTIALS) String userCredentials)
        throws InvalidParameterException {
        logger.info("Deleting job with Id " + jobId + ".");

        User user;

        try {
            user = this.authService.authorizeUser(userCredentials);
        } catch (UnauthorizedRequestException ure) {
            return new ResponseEntity<>(
                "The authentication failed with error [" + ure.getMessage() +
                    "]", HttpStatus.UNAUTHORIZED);
        }

        String stoppedJobId = this.jobService.stopJob(jobId, user.getUserIdentification());

        if (stoppedJobId == null) {
            logger.info(
                "Could not find job with id " + jobId + " for user " + user
                    .getUserIdentification());
            throw new InvalidParameterException("Could not find job with id '" + jobId + "'.");
        }

        return new ResponseEntity<>(new SimpleJobResponse(stoppedJobId), HttpStatus.ACCEPTED);
    }

    private List<TaskDTO> toTasksDTOList(List<Task> tasks) {
        List<TaskDTO> l = new ArrayList<>();
        for (Task t : tasks) {
            l.add(new TaskDTO(t));
        }
        return l;
    }

    private JDFJob getJDFJob(String jobId, String userCredentials)
        throws InvalidParameterException, UnauthorizedRequestException {
        User user = this.authService.authorizeUser(userCredentials);
        JDFJob job = this.jobService.getJobById(jobId, user);

        if (job == null) {
            job = this.jobService.getJobByName(jobId, user.getUserIdentification());
            if (job == null) {
                logger.info(
                    "Could not find job with id " + jobId + " for user " + user
                        .getUserIdentification());
                throw new InvalidParameterException("Could not find job with id '" + jobId + "'.");
            }
        }
        return job;
    }

    public class SimpleJobResponse {

        private String id;

        SimpleJobResponse(String id) {
            this.id = id;
        }

        public String getId() {
            return this.id;
        }

        public void setIt(String id) {
            this.id = id;
        }
    }
}
