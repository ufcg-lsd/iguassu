package org.fogbowcloud.app.api.http.services;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.api.http.controllers.QueueAPI;
import org.fogbowcloud.app.core.ApplicationFacade;
import org.fogbowcloud.app.core.constants.AppConstant;
import org.fogbowcloud.app.core.exceptions.JobNotFoundException;
import org.fogbowcloud.app.core.exceptions.StorageException;
import org.fogbowcloud.app.core.exceptions.UnauthorizedRequestException;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.core.models.job.JobState;
import org.fogbowcloud.app.core.models.user.User;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Lazy
@Service
public class JobService {
    private final Logger logger = Logger.getLogger(JobService.class);

    @Lazy
    private final FileStorageService storageService;

    private ApplicationFacade applicationFacade = ApplicationFacade.getInstance();

    public JobService(FileStorageService storageService) {
        this.storageService = storageService;
    }

    public Collection<Job> getActiveJobsFromQueueByUser(String queueId, User user) {
        return this.applicationFacade.findAllJobsFromQueueByUserId(queueId, user.getId())
                .stream()
                .filter(
                        job -> !job.getState().equals(JobState.REMOVED)
                ).collect(Collectors.toList());
    }

    public Job getJobFromQueueById(String queueId, String jobId, User user) throws JobNotFoundException, UnauthorizedRequestException {
        return this.applicationFacade.findJobFromQueueById(queueId, jobId, user);
    }

    public String removeJob(String jobId, Long userId) throws UnauthorizedRequestException {
        return this.applicationFacade.removeJob(jobId, userId);
    }

    public String submitJob(String queueId, MultipartFile rawJDF, User user) throws CompilerException {
        final Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put(AppConstant.JDF_FILE_PATH, null);

        this.storageService.store(rawJDF, fieldMap);

        final String jdfAbsolutePath = fieldMap.get(AppConstant.JDF_FILE_PATH);
        if (Objects.isNull(jdfAbsolutePath)) {
            logger.info("Could not store new jdf from user " + user.getAlias());
            throw new StorageException("Could not store new job from user " + user.getAlias());
        }

        String jobId;
        try {
            logger.info("Job description file path's <" + jdfAbsolutePath + ">");
            jobId = this.applicationFacade.submitJob(queueId, jdfAbsolutePath, user);
            logger.info("Job " + jobId + " created at time: " + System.currentTimeMillis());
        } catch (CompilerException ce) {
            logger.error(ce.getMessage(), ce);
            throw new CompilerException("Could not compile JDF file", ce);
        }

        return jobId;
    }
}
