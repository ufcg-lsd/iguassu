package org.fogbowcloud.app.core.datastore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import org.fogbowcloud.app.core.task.Task;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestJobDataStore {

    private static final String OWNER = "owner";
    private static final String DATASTORE_URL = "jdbc:h2:/tmp/jobdb";

    private JobDataStore datastore;

    @Before
    public void setUp() {
        this.datastore = new JobDataStore(DATASTORE_URL);
    }

    @After
    public void tearDown() {
        this.datastore.deleteAll();
    }

    @Test
    public void testAddJob() {
        JDFJob job = new JDFJob(OWNER, new ArrayList<Task>(), null);
        this.datastore.insert(job);
        assertEquals(1, this.datastore.getAll().size());
        assertEquals(job.getId(), this.datastore.getAll().get(0).getId());
    }

    @Test
    public void testGetAllJobs() {
        JDFJob job = new JDFJob(OWNER, new ArrayList<Task>(), null);
        JDFJob job2 = new JDFJob(OWNER, new ArrayList<Task>(), null);
        JDFJob job3 = new JDFJob(OWNER, new ArrayList<Task>(), null);

        this.datastore.insert(job);
        this.datastore.insert(job2);
        this.datastore.insert(job3);

        assertEquals(3, this.datastore.getAll().size());
        assertEquals(this.datastore.getByJobId(job.getId(), OWNER).getId(), job.getId());
        assertEquals(this.datastore.getByJobId(job2.getId(), OWNER).getId(), job2.getId());
        assertEquals(this.datastore.getByJobId(job3.getId(), OWNER).getId(), job3.getId());

    }

    @Test
    public void testDeleteJob() {
        JDFJob job = new JDFJob(OWNER, new ArrayList<Task>(), null);
        JDFJob job2 = new JDFJob(OWNER, new ArrayList<Task>(), null);
        JDFJob job3 = new JDFJob(OWNER, new ArrayList<Task>(), null);

        this.datastore.insert(job);
        this.datastore.insert(job2);
        this.datastore.insert(job3);

        assertEquals(3, this.datastore.getAll().size());
        assertEquals(this.datastore.getByJobId(job.getId(), OWNER).getId(), job.getId());
        assertEquals(this.datastore.getByJobId(job2.getId(), OWNER).getId(), job2.getId());
        assertEquals(this.datastore.getByJobId(job3.getId(), OWNER).getId(), job3.getId());

        this.datastore.deleteByJobId(job.getId(), OWNER);

        assertNull(this.datastore.getByJobId(job.getId(), OWNER));
    }

    /*
     * Test if the update operation does not insert a job already deleted, this can avoid a data
     * race between stop and update job operations.
     */
    @Test
    public void testUpdateDeletedJob() {
        JDFJob job = new JDFJob(OWNER, new ArrayList<Task>(), null);

        this.datastore.insert(job);
        Assert.assertEquals(job, this.datastore.getByJobId(job.getId(), job.getOwner()));

        this.datastore.deleteByJobId(job.getId(), job.getOwner());
        this.datastore.update(job);
        Assert.assertNull(this.datastore.getByJobId(job.getId(), job.getOwner()));
    }
}
