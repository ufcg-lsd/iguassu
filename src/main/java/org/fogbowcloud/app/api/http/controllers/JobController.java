package org.fogbowcloud.app.api.http.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.api.constants.ApiDocumentation;
import org.fogbowcloud.app.api.exceptions.StorageException;
import org.fogbowcloud.app.api.http.services.FileSystemStorageService;
import org.fogbowcloud.app.api.http.services.JobService;
import org.fogbowcloud.app.core.authenticator.models.User;
import org.fogbowcloud.app.core.constants.IguassuPropertiesConstants;
import org.fogbowcloud.app.core.dto.JobResponseDTO;
import org.fogbowcloud.app.core.exceptions.InvalidParameterException;
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
@RequestMapping(value = ApiDocumentation.Endpoint.JOB_ENDPOINT)
@Api(ApiDocumentation.Job.API_DESCRIPTION)
public class JobController {

    private final Logger LOGGER = Logger.getLogger(JobController.class);
    private final FileSystemStorageService storageService;
    @Lazy
    private JobService jobService;

    @Autowired
    public JobController(FileSystemStorageService storageService, JobService jobService) {
        this.storageService = storageService;
        this.jobService = jobService;
    }

    @GetMapping
    @ApiOperation(value = ApiDocumentation.Job.GET_OPERATION)
    public ResponseEntity<List<JobResponseDTO>> getAllJobs(
        @ApiParam(value = ApiDocumentation.CommonParameters.USER_CREDENTIALS)
        @RequestHeader(value = IguassuPropertiesConstants.X_CREDENTIALS) String credentials) {
        LOGGER.info("Retrieving all jobs.");

        User owner = this.jobService.authenticateUser(credentials);
        List<JDFJob> allJobs = this.jobService.getAllJobs(owner);
        List<JobResponseDTO> jobs = new LinkedList<>();
        for (JDFJob job : allJobs) {
            jobs.add(new JobResponseDTO(job));
        }

        return new ResponseEntity<>(jobs, HttpStatus.OK);
    }

    @GetMapping(value = ApiDocumentation.Endpoint.JOB_PATH)
    @ApiOperation(value = ApiDocumentation.Job.GET_BY_ID_OPERATION)
    public ResponseEntity<JobResponseDTO> getJobById(
        @ApiParam(value = ApiDocumentation.Job.ID)
        @PathVariable String jobId,
        @ApiParam(value = ApiDocumentation.CommonParameters.USER_CREDENTIALS)
        @RequestHeader(value = IguassuPropertiesConstants.X_CREDENTIALS) String credentials)
        throws InvalidParameterException {
        LOGGER.info("Retrieving job with id [" + jobId + "].");

        JDFJob job = getJDFJob(jobId, credentials);

        return new ResponseEntity<>(new JobResponseDTO(job), HttpStatus.OK);
    }

    @GetMapping(value = ApiDocumentation.Endpoint.JOB_PATH + "/"
        + ApiDocumentation.Endpoint.TASKS_ENDPOINT)
    @ApiOperation(value = ApiDocumentation.Job.GET_TASKS_OPERATION)
    public ResponseEntity<List<Task>> getJobTasks(
        @ApiParam(value = ApiDocumentation.Job.ID)
        @PathVariable String jobId,
        @ApiParam(value = ApiDocumentation.CommonParameters.USER_CREDENTIALS)
        @RequestHeader(value = IguassuPropertiesConstants.X_CREDENTIALS) String credentials)
        throws InvalidParameterException {
        LOGGER.info("Retrieving tasks from job with id [" + jobId + "].");

        JDFJob job = getJDFJob(jobId, credentials);

        return new ResponseEntity<>(job.getTasks(), HttpStatus.OK);
    }

    private JDFJob getJDFJob(String jobId, String credentials) throws InvalidParameterException {
        User owner = this.jobService.authenticateUser(credentials);
        JDFJob job = this.jobService.getJobById(jobId, owner);

        if (job == null) {
            job = this.jobService.getJobByName(jobId, owner.getUserIdentification());
            if (job == null) {
                LOGGER.info(
                    "Could not find job with id " + jobId + " for user " + owner
                        .getUserIdentification());
                throw new InvalidParameterException("Could not find job with id '" + jobId + "'.");
            }
        }
        return job;
    }

    @PostMapping
    @ApiOperation(value = ApiDocumentation.Job.CREATE_OPERATION)
    public ResponseEntity<String> submitJob(
        @ApiParam(value = ApiDocumentation.Job.CREATE_REQUEST_PARAM)
        @RequestParam(IguassuPropertiesConstants.JDF_FILE_PATH) MultipartFile file,
        @ApiParam(value = ApiDocumentation.CommonParameters.USER_CREDENTIALS)
        @RequestHeader(value = IguassuPropertiesConstants.X_CREDENTIALS) String credentials) {
        LOGGER.info("Saving new Job.");

        LOGGER.info(file.toString());

        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put(IguassuPropertiesConstants.JDF_FILE_PATH, null);
        fieldMap.put(IguassuPropertiesConstants.X_CREDENTIALS, null);

        this.storageService.store(file, fieldMap);
        User owner = this.jobService.authenticateUser(credentials);

        String jdf = fieldMap.get(IguassuPropertiesConstants.JDF_FILE_PATH);
        if (jdf == null) {
            LOGGER.info("Could not store  new job from user " + owner.getUserIdentification());
            throw new StorageException(
                "Could not store  new job from user " + owner.getUserIdentification());
        }

        String jobId;
        String jdfAbsolutePath = fieldMap.get(IguassuPropertiesConstants.JDF_FILE_PATH);
        try {
            LOGGER.info("jdfpath <" + jdfAbsolutePath + ">");
            jobId = this.jobService.submitJob(jdfAbsolutePath, owner);
            LOGGER.info("Job " + jobId + " created at time: " + System.currentTimeMillis());
        } catch (CompilerException ce) {
            LOGGER.error(ce.getMessage(), ce);
            throw new StorageException("Could not compile JDF file.", ce);
        } catch (IOException e) {
            LOGGER.error("Could not read JDF file.", e);
            throw new StorageException("Could not read JDF file.");
        }
        return new ResponseEntity<>(jobId, HttpStatus.CREATED);
    }

    @DeleteMapping(value = ApiDocumentation.Endpoint.JOB_PATH)
    @ApiOperation(value = ApiDocumentation.Job.DELETE_OPERATION)
    public ResponseEntity<SimpleJobResponse> stopJob(
        @ApiParam(value = ApiDocumentation.Job.ID)
        @PathVariable String jobId,
        @ApiParam(value = ApiDocumentation.CommonParameters.USER_CREDENTIALS)
        @RequestHeader(value = IguassuPropertiesConstants.X_CREDENTIALS) String credentials)
        throws InvalidParameterException {
        LOGGER.info("Deleting job with Id " + jobId + ".");

        User owner = this.jobService.authenticateUser(credentials);

        String stoppedJobId = this.jobService.stopJob(jobId, owner.getUserIdentification());

        if (stoppedJobId == null) {
            LOGGER.info(
                "Could not find job with id " + jobId + " for user " + owner
                    .getUserIdentification());
            throw new InvalidParameterException("Could not find job with id '" + jobId + "'.");
        }

        return new ResponseEntity<>(new SimpleJobResponse(stoppedJobId), HttpStatus.ACCEPTED);
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
