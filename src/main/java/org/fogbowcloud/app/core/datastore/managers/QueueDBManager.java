package org.fogbowcloud.app.core.datastore.managers;

import org.fogbowcloud.app.core.datastore.repositories.QueueRepository;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.core.models.queue.ArrebolQueue;
import org.fogbowcloud.app.core.models.user.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class QueueDBManager {

    public static final String DEFAULT_QUEUE_ID = "default";
    private static final ArrebolQueue DEFAULT_QUEUE = new ArrebolQueue(DEFAULT_QUEUE_ID, (long) -1, new ArrayList<>(),
            "Default", new ArrayList<>());
    private static QueueDBManager instance;
    private QueueRepository queueRepository;

    private QueueDBManager() {
    }

    public synchronized static QueueDBManager getInstance() {
        if (instance == null) {
            instance = new QueueDBManager();
        }
        return instance;
    }

    public void init() {
        this.queueRepository.save(DEFAULT_QUEUE);
    }

    public ArrebolQueue findOne(String queueId) {
        return this.queueRepository.findById(queueId).isPresent() ? this.queueRepository
                .findById(queueId).get() : null;
    }

    public List<ArrebolQueue> findAll() {
        return this.queueRepository.findAll();
    }

    public void update(ArrebolQueue queue) {
        this.queueRepository.save(queue);
    }

    public void setQueueRepository(QueueRepository queueRepository) {
        this.queueRepository = queueRepository;
    }

    public void addJobToQueue(String queueId, Job job) {
        ArrebolQueue queue = this.findOne(queueId);
        queue.getJobs().add(job);
        this.queueRepository.save(queue);
    }

    public void save(String queueId, Long ownerId, String name) {
        List<Job> jobs = Collections.synchronizedList(new ArrayList<>());
        ArrebolQueue queue = new ArrebolQueue(queueId, ownerId, jobs, name, new ArrayList<>());
        this.queueRepository.save(queue);
    }

    public List<ArrebolQueue> getQueuesByUser(User user) {
        List<ArrebolQueue> queues = this.queueRepository.getArrebolQueuesByOwnerIdEquals(user.getId());
        queues.add(this.queueRepository.findById(DEFAULT_QUEUE_ID).get());
        return queues;
    }

    public boolean existsQueueFromUser(String queueId, User user) {
        if (queueId.equals(DEFAULT_QUEUE_ID)) {
            return true;
        }
        ArrebolQueue arrebolQueue = this.queueRepository.getArrebolQueueByQueueIdAndOwnerId(queueId, user.getId());
        return !Objects.isNull(arrebolQueue);
    }
}
