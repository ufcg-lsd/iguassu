package org.fogbowcloud.app.api.http.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.api.constants.ApiDocumentation;
import org.fogbowcloud.app.api.exceptions.StorageException;
import org.fogbowcloud.app.api.http.services.FileSystemStorageService;
import org.fogbowcloud.app.api.http.services.JobService;
import org.fogbowcloud.app.core.exceptions.InvalidParameterException;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.core.authenticator.models.User;
import org.fogbowcloud.app.core.constants.IguassuPropertiesConstants;
import org.fogbowcloud.blowout.core.exception.BlowoutException;
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
@RequestMapping(value = JobController.JOB_ENDPOINT)
@Api(description = ApiDocumentation.Job.API)
public class JobController {

    protected static final String JOB_ENDPOINT = "job";
    private final String JOB_PATH = "/{jobId}";
    public static final String JDF_FILE_PATH = "jdffilepath";

    private final Logger LOGGER = Logger.getLogger(JobController.class);

    @Lazy
    JobService jobService;

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
        List<JDFJob> list = this.jobService.getAllJobs(owner);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @RequestMapping(value = JOB_PATH, method = RequestMethod.GET)
    @ApiOperation(value = ApiDocumentation.Job.GET_BY_ID_OPERATION)
    public ResponseEntity<JDFJob> getJobById(
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

        return new ResponseEntity<>(job, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(value = ApiDocumentation.Job.CREATE_OPERATION)
    public ResponseEntity<JobResponse> addJob(
            @ApiParam(value = ApiDocumentation.Job.CREATE_REQUEST_PARAM)
                    @RequestParam(JobController.JDF_FILE_PATH) MultipartFile file, RedirectAttributes redirectAttributes,
            @ApiParam(value = ApiDocumentation.CommonParameters.CREDENTIALS)
                    @RequestHeader(value=IguassuPropertiesConstants.X_CREDENTIALS) String credentials) {
        LOGGER.info("Saving new Job.");

        LOGGER.info(file.toString());

        // Credentials
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put(JDF_FILE_PATH, null);
        fieldMap.put(IguassuPropertiesConstants.X_CREDENTIALS, null);

        // handle file upload
        this.storageService.store(file, fieldMap);
        User owner = this.jobService.authenticateUser(credentials);

        // Creating job
        String jdf = fieldMap.get(JDF_FILE_PATH);
        if (jdf == null) {
            LOGGER.info("Could not store  new job from user " + owner.getUsername());
            throw new StorageException("Could not store  new job from user " + owner.getUsername());
        }

        String jobId;
        String jdfAbsolutePath = fieldMap.get(JDF_FILE_PATH);
        try {
            LOGGER.info("jdfpath <" + jdfAbsolutePath + ">");
            jobId = this.jobService.addJob(jdfAbsolutePath, owner);
            LOGGER.info("Job "+ jobId + " created at time: "+ System.currentTimeMillis() );
        } catch (CompilerException ce) {
            LOGGER.error(ce.getMessage(), ce);
            throw new StorageException("Could not compile JDF file.", ce);
        } catch (BlowoutException iae) {
            LOGGER.error(iae.getMessage(), iae);
            throw new StorageException(iae.getMessage());
        } catch (IOException e) {
            LOGGER.error("Could not read JDF file.", e);
            throw new StorageException("Could not read JDF file.");
        }

        JobResponse mJobId = new JobResponse(jobId);
        return new ResponseEntity<>(mJobId, HttpStatus.CREATED);
    }

    @RequestMapping(value = JOB_PATH, method = RequestMethod.DELETE)
    @ApiOperation(value = ApiDocumentation.Job.DELETE_OPERATION)
    public ResponseEntity<JobResponse> stopJob(
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

        JobResponse jobResponse = new JobResponse(stoppedJobId);
        return new ResponseEntity<>(jobResponse, HttpStatus.OK);
    }

    public class JobResponse {
        private String id;
        public JobResponse(String id) { this.id = id; }
        public String getId() {
            return this.id;
        }
        public void setIt(String id) {
            this.id = id;
        }
    }

}
