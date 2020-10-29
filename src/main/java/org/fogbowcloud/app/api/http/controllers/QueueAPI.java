package org.fogbowcloud.app.api.http.controllers;

import io.swagger.annotations.*;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.api.constants.Documentation;
import org.fogbowcloud.app.api.dtos.*;
import org.fogbowcloud.app.api.http.services.AuthService;
import org.fogbowcloud.app.api.http.services.JobService;
import org.fogbowcloud.app.api.http.services.QueueService;
import org.fogbowcloud.app.core.constants.AppConstant;
import org.fogbowcloud.app.core.exceptions.JobNotFoundException;
import org.fogbowcloud.app.core.exceptions.UnauthorizedRequestException;
import org.fogbowcloud.app.core.exceptions.UserNotExistException;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.core.models.task.Task;
import org.fogbowcloud.app.core.models.user.User;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.app.jes.arrebol.dtos.QueueDTO;
import org.fogbowcloud.app.ps.models.Pool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.*;

@RestController
@RequestMapping(value = Documentation.Endpoint.QUEUES)
@Api(Documentation.Queue.DESCRIPTION)
public class QueueAPI {

    private final Logger logger = Logger.getLogger(QueueAPI.class);

    @Lazy
    private JobService jobService;

    @Lazy
    private QueueService queueService;

    @Lazy
    private AuthService authService;

    @Autowired
    public QueueAPI(JobService jobService, AuthService authService, QueueService queueService) {
        this.jobService = jobService;
        this.authService = authService;
        this.queueService = queueService;
    }

    @PostMapping
    @ApiOperation(value = Documentation.Queue.CREATE_QUEUE, produces = "application/json")
    public ResponseEntity<?> addQueue(
            @Valid @RequestBody QueueDTORequest queueDTORequest,
            @ApiParam(value = Documentation.CommonParameters.USER_CREDENTIALS)
            @RequestHeader(value = AppConstant.X_AUTH_USER_CREDENTIALS) @Valid @NotBlank String userCredentials) {

        User user;

        try {
            user = this.authService.authorizeUser(userCredentials);
        } catch (UnauthorizedRequestException | UserNotExistException ure) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("The authentication failed with error [" + ure + "]");
        }

        String queueId;
        try {
            queueId = this.queueService.createQueue(user, queueDTORequest);
            Map<String, String> body = new HashMap<>();
            body.put("id", queueId);
            URI location = ServletUriComponentsBuilder
                    .fromCurrentContextPath().path(Documentation.Endpoint.QUEUE)
                    .buildAndExpand(queueId).toUri();
            return ResponseEntity.created(location).body(body);
        } catch (Throwable t) {
            return ResponseEntity.badRequest().body(String.format("Operation returned error: %s", t.getMessage()));
        }
    }

    @GetMapping
    @ApiOperation(value = Documentation.Queue.RETRIEVES_QUEUES)
    public ResponseEntity<?> getQueues(
            @RequestHeader(value = AppConstant.X_AUTH_USER_CREDENTIALS)
            @ApiParam(value = Documentation.CommonParameters.USER_CREDENTIALS)
            @Valid @NotBlank String userCredentials) {

        User user;

        try {
            user = this.authService.authorizeUser(userCredentials);
        } catch (UnauthorizedRequestException | UserNotExistException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("The authentication failed with error [" + e + "]");
        }

        List<QueueDTO> queues;
        try {
            queues = this.queueService.getQueues(user);
            return ResponseEntity.ok(queues);
        } catch (Throwable t) {
            return ResponseEntity.badRequest().body(String.format("Operation returned error: %s", t.getMessage()));
        }
    }

    @GetMapping(Documentation.Endpoint.QUEUE)
    @ApiOperation(value = Documentation.Queue.RETRIEVE_QUEUE)
    public ResponseEntity<?> getQueue(
            @ApiParam(value = Documentation.CommonParameters.USER_CREDENTIALS)
            @RequestHeader(value = AppConstant.X_AUTH_USER_CREDENTIALS) @Valid @NotBlank String userCredentials,
            @ApiParam(value = Documentation.Queue.QUEUE_ID) @PathVariable @Valid @NotBlank String queueId) {

        User user;

        try {
            user = this.authService.authorizeUser(userCredentials);
        } catch (UnauthorizedRequestException | UserNotExistException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("The authentication failed with error [" + e + "]");
        }

        QueueDTOResponse response;
        try {
            response = this.queueService.getQueue(user, queueId);
            return ResponseEntity.ok(response);
        } catch (UnauthorizedRequestException ure) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(value = Documentation.Endpoint.SUBMIT_JOB)
    @ApiOperation(value = Documentation.Queue.SUBMIT_JOB)
    public ResponseEntity<?> submitJob(
            @ApiParam(value = Documentation.Queue.CREATE_REQUEST_PARAM)
            @RequestParam(AppConstant.JDF_FILE_PATH) @Valid @NotNull MultipartFile rawJDF,

            @ApiParam(value = Documentation.Queue.QUEUE_ID) @PathVariable @Valid @NotBlank String queueId,

            @ApiParam(value = Documentation.CommonParameters.USER_CREDENTIALS)
            @RequestHeader(value = AppConstant.X_AUTH_USER_CREDENTIALS)
            @Valid @NotBlank String userCredentials) {

        if (Objects.isNull(queueId)) {
            return ResponseEntity.badRequest().body(new InvalidRequestDTO("No queued specified"));
        }

        User user;

        try {
            user = this.authService.authorizeUser(userCredentials);
        } catch (UnauthorizedRequestException | UserNotExistException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("The authentication failed with error [" + e + "]");
        }

        String jobId;
        try {
            jobId = this.jobService.submitJob(queueId, rawJDF, user);
        } catch (CompilerException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(String.format("Operation returned error: %s", e.getMessage()));
        }

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/{jobId}")
                .buildAndExpand(jobId).toUri();
        return ResponseEntity.created(location).body(new SimpleJobResponse(jobId));
    }

    @GetMapping(value = Documentation.Endpoint.RETRIEVE_ALL_JOBS)
    @ApiOperation(value = Documentation.Queue.RETRIEVE_ALL_JOBS)
    public ResponseEntity<?> getAllJobs(
            @ApiParam(value = Documentation.Queue.QUEUE_ID) @Valid @NotBlank @PathVariable String queueId,

            @ApiParam(value = Documentation.CommonParameters.USER_CREDENTIALS)
            @RequestHeader(value = AppConstant.X_AUTH_USER_CREDENTIALS) @Valid @NotBlank String credentials) {
        logger.info("Request to retrieve all jobs per user received");

        User user;

        try {
            user = this.authService.authorizeUser(credentials);
        } catch (UnauthorizedRequestException | UserNotExistException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("The authentication failed with error [" + e + "]");
        }

        final Collection<Job> allJobsOfUser = this.jobService.getActiveJobsFromQueueByUser(queueId, user);

        final List<JobDTO> response = new LinkedList<>();

        allJobsOfUser.forEach(job -> response.add(new JobDTO(job, queueId)));

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = Documentation.Endpoint.RETRIEVE_JOB_BY_ID)
    @ApiOperation(value = Documentation.Queue.RETRIEVE_JOB_BY_ID)
    public ResponseEntity<?> getJobById(
            @ApiParam(value = Documentation.Queue.QUEUE_ID) @Valid @NotBlank @PathVariable String queueId,

            @ApiParam(value = Documentation.Queue.JOB_ID) @Valid @NotBlank @PathVariable String jobId,

            @ApiParam(value = Documentation.CommonParameters.USER_CREDENTIALS)
            @RequestHeader(value = AppConstant.X_AUTH_USER_CREDENTIALS)
            @Valid @NotBlank String userCredentials) throws UserNotExistException {

        Job job;
        try {
            job = getJDFJob(queueId, jobId, userCredentials);

        } catch (UnauthorizedRequestException ure) {
            return new ResponseEntity<>(
                    "The authentication failed with error [" + ure.getMessage() + "]",
                    HttpStatus.UNAUTHORIZED);
        } catch (JobNotFoundException e) {
            return new ResponseEntity<>(
                    "Job " + jobId + "not found [" + e.getMessage() + "]",
                    HttpStatus.NOT_FOUND);
        }
        logger.info("Retrieving job with id [" + jobId + "]");
        return new ResponseEntity<>(new JobDTO(job, queueId), HttpStatus.OK);
    }

    @GetMapping(value = Documentation.Endpoint.RETRIEVE_TASKS_BY_JOB)
    @ApiOperation(value = Documentation.Queue.RETRIEVE_TASKS_BY_JOB)
    public ResponseEntity<?> getJobTasks(
            @ApiParam(value = Documentation.Queue.QUEUE_ID) @Valid @NotBlank @PathVariable String queueId,

            @ApiParam(value = Documentation.Queue.JOB_ID) @Valid @NotBlank @PathVariable String jobId,

            @ApiParam(value = Documentation.CommonParameters.USER_CREDENTIALS)
            @RequestHeader(value = AppConstant.X_AUTH_USER_CREDENTIALS)
            @Valid @NotBlank String userCredentials) {

        Job job;
        try {
            job = getJDFJob(queueId, jobId, userCredentials);

        } catch (UnauthorizedRequestException | UserNotExistException ure) {
            return new ResponseEntity<>(
                    "The authentication failed with error [" + ure + "]",
                    HttpStatus.UNAUTHORIZED);
        } catch (JobNotFoundException e) {
            return new ResponseEntity<>(
                    "Job " + jobId + "not found [" + e.getMessage() + "]",
                    HttpStatus.NOT_FOUND);
        }
        logger.info("Retrieving tasks from job with id [" + jobId + "]");
        Collection<TaskDTO> response = generateTaskList(job.getTasks());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping(value = Documentation.Endpoint.DELETE_JOB_BY_ID)
    @ApiOperation(value = Documentation.Queue.DELETE_JOB_BY_ID)
    public ResponseEntity<?> stopJob(
            @ApiParam(value = Documentation.Queue.QUEUE_ID) @Valid @NotBlank @PathVariable String queueId,

            @ApiParam(value = Documentation.Queue.JOB_ID) @Valid @NotBlank @PathVariable String jobId,

            @ApiParam(value = Documentation.CommonParameters.USER_CREDENTIALS)
            @RequestHeader(value = AppConstant.X_AUTH_USER_CREDENTIALS) @Valid @NotBlank String userCredentials) {

        logger.info("Deleting job with Id " + jobId + ".");

        User user;

        try {
            user = this.authService.authorizeUser(userCredentials);
        } catch (UnauthorizedRequestException | UserNotExistException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("The authentication failed with error [" + e + "]");
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

    @PostMapping(value = Documentation.Endpoint.NODE_ENDPOINT)
    @ApiOperation(value = Documentation.Queue.SUBMIT_NODES)
    public ResponseEntity<?> addWorkers(
            @ApiParam(value = Documentation.CommonParameters.USER_CREDENTIALS)
            @RequestHeader(value = AppConstant.X_AUTH_USER_CREDENTIALS) @Valid @NotBlank String userCredentials,

            @ApiParam(value = Documentation.Queue.QUEUE_ID) @Valid @NotBlank @PathVariable String queueId,
            @Valid @RequestBody ResourceNode resourceNode) {

        User user;
        try {
            user = this.authService.authorizeUser(userCredentials);
        } catch (UnauthorizedRequestException | UserNotExistException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("The authentication failed with error [" + e + "]");
        }

        Pool pool;
        try {
            pool = queueService.addNode(user, queueId, resourceNode);
            return new ResponseEntity<>(pool, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error(String.format("Operation returned error: %s", e.getMessage()), e);
            return new ResponseEntity<>(
                    "The authentication failed with error [" + e.getMessage() + "]",
                    HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping(value = Documentation.Endpoint.NODE_ENDPOINT)
    @ApiOperation(value = Documentation.Queue.RETRIEVES_NODES)
    public ResponseEntity<?> getNodes(
            @ApiParam(value = Documentation.CommonParameters.USER_CREDENTIALS)
            @RequestHeader(value = AppConstant.X_AUTH_USER_CREDENTIALS) @Valid @NotBlank String userCredentials,

            @ApiParam(value = Documentation.Queue.QUEUE_ID) @Valid @NotBlank @PathVariable String queueId) {

        User user;
        try {
            user = this.authService.authorizeUser(userCredentials);
        } catch (UnauthorizedRequestException | UserNotExistException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("The authentication failed with error [" + e + "]");
        }

        Pool pool;
        try {
            pool = queueService.getNodes(user, queueId);
            return new ResponseEntity<>(pool, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(String.format("Operation returned error: %s", e.getMessage()), e);
            return new ResponseEntity<>(
                    "The authentication failed with error [" + e.getMessage() + "]",
                    HttpStatus.UNAUTHORIZED);
        }
    }

    private Collection<TaskDTO> generateTaskList(Collection<Task> tasks) {
        final Collection<TaskDTO> l = new ArrayList<>();
        for (Task t : tasks) {
            l.add(new TaskDTO(t));
        }
        return l;
    }

    private Job getJDFJob(String queueId, String jobId, String userCredentials)
            throws UnauthorizedRequestException, JobNotFoundException, UserNotExistException {
        final User user;
        try {
            user = this.authService.authorizeUser(userCredentials);
        } catch (UserNotExistException e) {
            throw new UserNotExistException();
        }

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
