package org.fogbowcloud.app.core.datastore.repositories;

import org.fogbowcloud.app.core.models.queue.Queue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueueRepository extends JpaRepository<Queue, String> {

}
