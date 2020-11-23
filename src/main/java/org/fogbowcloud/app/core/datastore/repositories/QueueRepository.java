package org.fogbowcloud.app.core.datastore.repositories;

import java.util.List;
import org.fogbowcloud.app.core.models.queue.ArrebolQueue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueueRepository extends JpaRepository<ArrebolQueue, String> {

    List<ArrebolQueue> getArrebolQueuesByOwnerIdEquals(Long ownerId);

    ArrebolQueue getArrebolQueueByQueueIdAndOwnerId(String queueId, Long ownerId);

}
