package org.fogbowcloud.app.core.datastore.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.fogbowcloud.app.core.datastore.repositories.QueueRepository;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.core.models.queue.Queue;
import org.fogbowcloud.app.utils.Pair;

public class QueueDBManager {

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

    public Queue findOne(String queueId) {
        return this.queueRepository.findById(queueId).isPresent() ? this.queueRepository
            .findById(queueId).get() : null;
    }

    public List<Queue> findAll() {
        return this.queueRepository.findAll();
    }

    public void update(Queue queue) {
        this.queueRepository.save(queue);
    }

    public void setQueueRepository(QueueRepository queueRepository) {
        this.queueRepository = queueRepository;
    }

    public synchronized void save(Pair<String, Job> pair) {
        String queueId = pair.getKey();
        Job job = pair.getValue();
        Queue queue = this.findOne(queueId);
        if(Objects.isNull(queue)){
            queue = createQueue(queueId);
        }
        queue.getJobs().add(job);
        this.queueRepository.save(queue);
    }

    private Queue createQueue(String queueId) {
        List<Job> jobs = Collections.synchronizedList(new ArrayList<>());
        Queue queue = new Queue(queueId, jobs);
        return queue;
    }
}
