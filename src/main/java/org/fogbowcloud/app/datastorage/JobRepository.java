package org.fogbowcloud.app.datastorage;

import org.fogbowcloud.app.core.models.job.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, String> {}
