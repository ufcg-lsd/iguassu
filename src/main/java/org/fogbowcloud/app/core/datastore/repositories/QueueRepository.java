package org.fogbowcloud.app.core.datastore.repositories;

import org.fogbowcloud.app.core.models.queue.ArrebolQueue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QueueRepository extends JpaRepository<ArrebolQueue, String> {

}
