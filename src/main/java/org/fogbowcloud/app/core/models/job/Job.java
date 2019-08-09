package org.fogbowcloud.app.core.models.job;

import org.fogbowcloud.app.core.models.task.Task;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "job")
public class Job implements Serializable {
    private static final String USER_ID_COLUMN_NAME = "user_id";
    private static final String JOB_ID_COLUMN_NAME = "job_id";
    private static final String TASK_ID_COLUMN_NAME = "task_id";
    private static final String STATE_COLUMN_NAME = "state";
    private static final String TIMESTAMP_COLUMN_NAME = "timestamp";
    private static final String LABEL_COLUMN_NAME = "label";
    private static final String EXECUTION_ID_COLUMN_NAME = "execution_id";
    private static final String TASKS_COLUMN_NAME = "tasks";

    @Id @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Column(name = USER_ID_COLUMN_NAME)
    private Long ownerId;

    @ElementCollection
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyColumn(name = TASK_ID_COLUMN_NAME)
    @JoinColumn(name = JOB_ID_COLUMN_NAME)
    @Column(name = TASKS_COLUMN_NAME)
    private Map<Long, Task> tasks;

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

    public Job(Map<Long, Task> tasks, String label, Long ownerId) {
        this.state = JobState.CREATED;
        this.tasks = tasks;
        this.label = label.trim().isEmpty() ? ownerId + "_job" : label;
        this.ownerId = ownerId;
        this.timestamp = Instant.now().getEpochSecond();
    }

    public Long getId() {
        return id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Map<Long, Task> getTasks() {
        return tasks;
    }

    public void setTasks(Map<Long, Task> tasks) {
        this.tasks = tasks;
    }

    public List<Task> getTasksAsList() {
        return new ArrayList<>(tasks.values());
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
