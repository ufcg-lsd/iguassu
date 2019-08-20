package org.fogbowcloud.app.core.models.job;

import org.fogbowcloud.app.core.models.task.Task;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "job")
public class Job implements Serializable {
    private static final String USER_ID_COLUMN_NAME = "user_id";
    public static final int ID_FIXED_SIZE = 36;
    private static final String STATE_COLUMN_NAME = "state";
    private static final String TIMESTAMP_COLUMN_NAME = "timestamp";
    private static final String LABEL_COLUMN_NAME = "label";
    private static final String EXECUTION_ID_COLUMN_NAME = "execution_id";

    @Column
    @Id
    @Size(max = ID_FIXED_SIZE)
    private String id;

    @Column(name = USER_ID_COLUMN_NAME)
    private Long ownerId;

    @ElementCollection
    @OneToMany(fetch= FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private List<Task> tasks;

    @Column(name = STATE_COLUMN_NAME)
    @Enumerated(EnumType.STRING)
    private JobState state;

    @Column(name = TIMESTAMP_COLUMN_NAME)
    private Long timestamp;

    @Column(name = LABEL_COLUMN_NAME)
    private String label;

    @Column(name = EXECUTION_ID_COLUMN_NAME)
    private String executionId;

    public Job() {
    }

    public Job(List<Task> tasks, String label, Long ownerId) {
        this.id = UUID.randomUUID().toString();
        this.state = JobState.SUBMITTED;
        this.tasks = tasks;
        this.label = Objects.isNull(label) || label.trim().isEmpty() ? ownerId + "_job" : label;
        this.ownerId = ownerId;
        this.timestamp = Instant.now().getEpochSecond();
    }

    public String getId() {
        return id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Task getTaskById(Long taskId) {
        for (Task task : this.tasks) {
            if (task.getId().equals(taskId)) return task;
        }
        return null;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Task> getTasks() {
        return this.tasks;
    }

    public JobState getState() {
        return state;
    }

    public void setState(JobState state) {
        this.state = state;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return id.equals(job.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
