package org.fogbowcloud.app.api.dtos;

import io.swagger.annotations.ApiModelProperty;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.core.models.job.JobState;
import org.fogbowcloud.app.core.models.queue.ArrebolQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QueueDTOResponse {

    @ApiModelProperty(notes = "The Queue ID", position = 1, example = "d73b30b9-18b9-4274-b42a-e96a6f001458")
    private String id;

    @ApiModelProperty(notes = "The Queue Name", position = 2, example = "My Queue")
    private String name;

    @ApiModelProperty(notes = "The list of workers' host addresses", position = 3, example = "[\"10.30.11.19\",\"10.30.11.20\",\"10.30.1.30\"]")
    private List<String> pool;

    @ApiModelProperty(notes = "The list of jobs in the queue", position = 4, dataType = "JobDTO")
    private List<JobDTO> jobs;

    public QueueDTOResponse(ArrebolQueue arrebolQueue) {
        this.id = arrebolQueue.getQueueId();
        this.name = arrebolQueue.getName();
        this.pool = new ArrayList<>();
        this.jobs = new ArrayList<>();
        parseCollections(arrebolQueue.getJobs(), arrebolQueue.getPool());
    }

    public QueueDTOResponse() {}

    private void parseCollections(List<Job> jobs, List<String> pool) {
        List<Job> tmp = jobs.stream()
                .filter(
                        job -> !job.getState().equals(JobState.REMOVED)
                ).collect(Collectors.toList());
        for (Job job : tmp) {
            this.jobs.add(job.toDTO(this.id));
        }

        this.pool.addAll(pool);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getPool() {
        return pool;
    }

    public void setPool(List<String> pool) {
        this.pool = pool;
    }

    public List<JobDTO> getJobs() {
        return jobs;
    }

    public void setJobs(List<JobDTO> jobs) {
        this.jobs = jobs;
    }
}
