package org.fogbowcloud.app.api.http.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.api.constants.ApiDocumentation;
import org.fogbowcloud.app.api.exceptions.StorageException;
import org.fogbowcloud.app.api.http.services.FileSystemStorageService;
import org.fogbowcloud.app.api.http.services.JobService;
import org.fogbowcloud.app.core.dto.JobResponseDTO;
import org.fogbowcloud.app.core.exceptions.InvalidParameterException;
import org.fogbowcloud.app.jdfcompiler.job.JDFJobState;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.core.authenticator.models.User;
import org.fogbowcloud.app.core.constants.IguassuPropertiesConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping(value = ApiDocumentation.ApiEndpoints.JOB_ENDPOINT)
@Api(ApiDocumentation.Job.API)
public class JobController {
    private final Logger LOGGER = Logger.getLogger(JobController.class);

    @Lazy
    private JobService jobService;
    private final FileSystemStorageService storageService;

    @Autowired
    public JobController(FileSystemStorageService storageService, JobService jobService) {
        this.storageService = storageService;
        this.jobService = jobService;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = ApiDocumentation.Job.GET_OPERATION)
    public ResponseEntity<List<JDFJob>> getAllJobs(
            @ApiParam(value = ApiDocumentation.CommonParameters.CREDENTIALS)
                @RequestHeader(value=IguassuPropertiesConstants.X_CREDENTIALS) String credentials) {
        LOGGER.info("Retrieving all jobs.");

        User owner = this.jobService.authenticateUser(credentials);
        List<JDFJob> allJobs = this.jobService.getAllJobs(owner);

        return new ResponseEntity<>(allJobs, HttpStatus.OK);
    }

    @RequestMapping(value = ApiDocumentation.ApiEndpoints.JOB_PATH, method = RequestMethod.GET)
    @ApiOperation(value = ApiDocumentation.Job.GET_BY_ID_OPERATION)
    public ResponseEntity<JobResponseDTO> getJobById(
            @ApiParam(value = ApiDocumentation.Job.ID)
                @PathVariable String jobId,
            @ApiParam(value = ApiDocumentation.CommonParameters.CREDENTIALS)
                @RequestHeader(value=IguassuPropertiesConstants.X_CREDENTIALS) String credentials) throws InvalidParameterException {
        LOGGER.info("Retrieving job with id " + jobId + "].");

        User owner = this.jobService.authenticateUser(credentials);
        JDFJob job = this.jobService.getJobById(jobId, owner);

        if (job == null) {
            job = this.jobService.getJobByName(jobId, owner.getUser());
            if (job == null) {
                LOGGER.info("Could not find job with id " + jobId + " for user " + owner.getUsername());
                throw new InvalidParameterException("Could not find job with id '" + jobId + "'.");
            }
        }

        return new ResponseEntity<>(new JobResponseDTO(job), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(value = ApiDocumentation.Job.CREATE_OPERATION)
    public ResponseEntity<String> addJob(
            @ApiParam(value = ApiDocumentation.Job.CREATE_REQUEST_PARAM)
                    @RequestParam(IguassuPropertiesConstants.JDF_FILE_PATH) MultipartFile file, RedirectAttributes redirectAttributes,
            @ApiParam(value = ApiDocumentation.CommonParameters.CREDENTIALS)
                    @RequestHeader(value=IguassuPropertiesConstants.X_CREDENTIALS) String credentials) {
        LOGGER.info("Saving new Job.");

        LOGGER.info(file.toString());

        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put(IguassuPropertiesConstants.JDF_FILE_PATH, null);
        fieldMap.put(IguassuPropertiesConstants.X_CREDENTIALS, null);

        this.storageService.store(file, fieldMap);
        User owner = this.jobService.authenticateUser(credentials);

        String jdf = fieldMap.get(IguassuPropertiesConstants.JDF_FILE_PATH);
        if (jdf == null) {
            LOGGER.info("Could not store  new job from user " + owner.getUsername());
            throw new StorageException("Could not store  new job from user " + owner.getUsername());
        }

        String jobId;
        String jdfAbsolutePath = fieldMap.get(IguassuPropertiesConstants.JDF_FILE_PATH);
        try {
            LOGGER.info("jdfpath <" + jdfAbsolutePath + ">");
            jobId = this.jobService.addJob(jdfAbsolutePath, owner);
            LOGGER.info("Job "+ jobId + " created at time: "+ System.currentTimeMillis() );
        } catch (CompilerException ce) {
            LOGGER.error(ce.getMessage(), ce);
            throw new StorageException("Could not compile JDF file.", ce);
        } catch (IOException e) {
            LOGGER.error("Could not read JDF file.", e);
            throw new StorageException("Could not read JDF file.");
        }
        return new ResponseEntity<>(jobId, HttpStatus.CREATED);
    }

    @RequestMapping(value = ApiDocumentation.ApiEndpoints.JOB_PATH, method = RequestMethod.DELETE)
    @ApiOperation(value = ApiDocumentation.Job.DELETE_OPERATION)
    public ResponseEntity<String> stopJob(
            @ApiParam(value = ApiDocumentation.Job.ID)
                @PathVariable String jobId,
            @ApiParam(value = ApiDocumentation.CommonParameters.CREDENTIALS)
                @RequestHeader(value=IguassuPropertiesConstants.X_CREDENTIALS) String credentials)
            throws InvalidParameterException {
        LOGGER.info("Deleting job with Id " + jobId + ".");

        User owner = this.jobService.authenticateUser(credentials);

        String stoppedJobId = this.jobService.stopJob(jobId, owner.getUser());

        if (stoppedJobId == null) {
            LOGGER.info("Could not find job with id " + jobId + " for user " + owner.getUsername());
            throw new InvalidParameterException("Could not find job with id '" + jobId + "'.");
        }

        return new ResponseEntity<>(stoppedJobId, HttpStatus.ACCEPTED);
    }
}
