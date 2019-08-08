package org.fogbowcloud.app.core.datastore.repositories;

import org.fogbowcloud.app.core.models.job.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, String> {

    List<Job> findAllByOwnerId(long ownerId);
}
