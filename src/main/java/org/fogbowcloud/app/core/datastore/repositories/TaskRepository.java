package org.fogbowcloud.app.core.datastore.repositories;

import org.fogbowcloud.app.core.models.task.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long > {
}
