package org.fogbowcloud.app.core;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.*;

import org.fogbowcloud.app.core.models.auth.DefaultUser;
import org.fogbowcloud.app.core.datastore.JobDataStore;
import org.fogbowcloud.app.core.models.task.Specification;
import org.fogbowcloud.app.core.models.task.Task;
import org.fogbowcloud.app.core.models.task.TaskState;
import org.fogbowcloud.app.core.models.job.JDFJob;
import org.fogbowcloud.app.core.models.auth.User;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TestIguassuController {

    private static final String FAKE_UUID = "1234";
    private static final String FAKE_USER_ID = "fake-user-id";
    private static final String FAKE_TASK_ID = "fake-task-id";
    private static final String FAKE_IMAGE_FLAVOR_NAME = "fake-docker-image";

    private IguassuController iguassuController;
    private JobDataStore dataStore;

    @Before
    public void setUp() throws Exception {
        Properties properties = new Properties();
        this.iguassuController = Mockito.spy(new IguassuController(properties));
        this.dataStore = Mockito.mock(JobDataStore.class);
    }

    @After
    public void tearDown() {
        this.dataStore.deleteAll();
    }

    @Test
    public void testRestart() throws JSONException {
        ArrayList<Task> taskList = new ArrayList<>();
        Specification spec = new Specification(
                FAKE_IMAGE_FLAVOR_NAME,
                FAKE_USER_ID
        );
        Task task = new Task(FAKE_TASK_ID, spec, FAKE_UUID);
        taskList.add(task);

        JDFJob job = new JDFJob(FAKE_USER_ID, taskList, "");
        job.finishCreation();
        this.iguassuController.getJobDataStore().insert(job);

        Assert.assertEquals(1, this.iguassuController.getAllJobs(FAKE_USER_ID).size());
        JDFJob job1 = this.iguassuController.getAllJobs(FAKE_USER_ID).get(0);
        assert (job1.equals(job));
        System.out.println(this.iguassuController.getAllJobs(FAKE_USER_ID).get(0).getTaskById(FAKE_TASK_ID).getSpecification().toJSON().toString());

        Specification spec2 = Specification.fromJSON(
                new JSONObject(
                        this.iguassuController
                                .getAllJobs(FAKE_USER_ID)
                                .get(0)
                                .getTaskById(FAKE_TASK_ID)
                                .getSpecification()
                                .toJSON()
                                .toString()
                )
        );
        Assert.assertEquals(1, this.iguassuController.getAllJobs(FAKE_USER_ID).get(0).getTasks().size());
        assert (spec.equals(spec2));
        assert (task.equals(this.iguassuController.getAllJobs(FAKE_USER_ID).get(0).getTasks().get(FAKE_TASK_ID)));
        assert (spec.equals(this.iguassuController.getAllJobs(FAKE_USER_ID).get(0).getTasks().get(FAKE_TASK_ID).getSpecification()));

    }

    @Test
    public void testGetJobById() {
        String jobId = "jobId00";
        JDFJob job = new JDFJob(jobId, new ArrayList<>(), FAKE_USER_ID);
        doReturn(job).when(dataStore).getByJobId(jobId, FAKE_USER_ID);
        assert (job.equals(this.iguassuController.getJobById(jobId, FAKE_USER_ID)));

    }

    @Test
    public void testAddJob() throws Exception {
        String jdfFilePath = "";
        User user = new DefaultUser("testuser", "'this is a test user'");

        JDFJob job = new JDFJob(user.getIdentifier(), new ArrayList<Task>(), user.getIdentifier());
        Mockito.doReturn(job).when(this.iguassuController).buildJob(
                Mockito.anyString(),
                Mockito.any(User.class)
        );

        String id = this.iguassuController.submitJob(jdfFilePath, user);
        Assert.assertEquals(id, job.getId());
        Mockito.verify(this.dataStore).insert(job);
        Assert.assertTrue(this.dataStore.getByJobId(id, user.getIdentifier()).equals(job));
    }

    @Test
    public void testGetAllJobs() {
        ArrayList<JDFJob> jobs = new ArrayList<>();
        ArrayList<Task> task = new ArrayList<>();
        jobs.add(new JDFJob("job1", task, FAKE_USER_ID));
        jobs.add(new JDFJob("job2", task, FAKE_USER_ID));
        jobs.add(new JDFJob("job3", task, FAKE_USER_ID));
        jobs.add(new JDFJob("job4", task, FAKE_USER_ID));

        doReturn(jobs).when(this.dataStore).getAllByUserId(FAKE_USER_ID);
        doNothing().when(this.iguassuController).updateJob(any(JDFJob.class));

        this.iguassuController.getAllJobs(FAKE_USER_ID);

        Assert.assertEquals(jobs.size(), this.iguassuController.getAllJobs(FAKE_USER_ID).size());
    }

    @Test
    public void testGetAllJobsWithoutAnotherUser() {
        ArrayList<JDFJob> jobs = new ArrayList<>();
        ArrayList<Task> task = new ArrayList<>();

        jobs.add(new JDFJob(FAKE_USER_ID, task, null));
        jobs.add(new JDFJob(FAKE_USER_ID, task, null));
        jobs.add(new JDFJob(FAKE_USER_ID, task, null));
        jobs.add(new JDFJob(FAKE_USER_ID, task, null));
        doReturn(jobs).when(this.dataStore).getAllByUserId(FAKE_USER_ID);

        Assert.assertEquals(0, this.iguassuController.getAllJobs("wrong user").size());

        jobs.add(new JDFJob(FAKE_USER_ID, task, null));
        jobs.add(new JDFJob(FAKE_USER_ID, task, null));
        jobs.add(new JDFJob(FAKE_USER_ID, task, null));
        jobs.add(new JDFJob(FAKE_USER_ID, task, null));

        Assert.assertEquals(0, this.iguassuController.getAllJobs("wrong user").size());

        JobDataStore jobDataStore = this.iguassuController.getJobDataStore();
        Assert.assertEquals(0, jobDataStore.getAllByUserId("wrong user").size());
    }

    @Test
    public void testGetJobByName() {
        String jobName = "jobName00";
        ArrayList<JDFJob> jobs = new ArrayList<>();
        ArrayList<Task> task = new ArrayList<>();
        JDFJob jdfJob = new JDFJob(FAKE_USER_ID, task, null);
        jdfJob.setLabel(jobName);
        jobs.add(jdfJob);
        jobs.add(new JDFJob(FAKE_USER_ID, task, null));
        jobs.add(new JDFJob(FAKE_USER_ID, task, null));

        jobs.add(new JDFJob(FAKE_USER_ID, task, null));
        jobs.add(new JDFJob(FAKE_USER_ID, task, null));

        doReturn(jobs).when(this.dataStore).getAllByUserId(FAKE_USER_ID);
    }

    @Test
    public void testStopJob() {
        String jobName = "jobName00";

        JDFJob jdfJob = new JDFJob(FAKE_USER_ID, new ArrayList<Task>(), null);
        jdfJob.setLabel(jobName);
        doReturn(true).when(this.dataStore).deleteByJobId(jdfJob.getId(), FAKE_USER_ID);
        doNothing().when(iguassuController).updateJob(any(JDFJob.class));
        // update DB Map
        this.iguassuController.stopJob(jobName, FAKE_USER_ID);

        Mockito.verify(this.dataStore).deleteByJobId(jdfJob.getId(), FAKE_USER_ID);
    }

    @Test
    public void testStopJobWithId() {
        ArrayList<JDFJob> jobs = new ArrayList<>();
        ArrayList<Task> task = new ArrayList<>();

        JDFJob jdfJob = new JDFJob(FAKE_USER_ID, task, null);
        jobs.add(jdfJob);
        jobs.add(new JDFJob("job1", task, FAKE_USER_ID));
        jobs.add(new JDFJob("job2",task,FAKE_USER_ID));

        doReturn(jobs).when(this.dataStore).getAllByUserId(FAKE_USER_ID);
        doReturn(true).when(this.dataStore).deleteByJobId(jdfJob.getId(), FAKE_USER_ID);
        doNothing().when(iguassuController).updateJob(any(JDFJob.class));
        doReturn(jdfJob).when(this.dataStore).getByJobId(jdfJob.getId(), FAKE_USER_ID);
        // update DB Map
        this.iguassuController.stopJob(jdfJob.getId(), FAKE_USER_ID);

        Mockito.verify(this.dataStore).deleteByJobId(jdfJob.getId(), FAKE_USER_ID);
    }

    @Test
    public void testGetTaskById() {

        Task task = new Task(FAKE_TASK_ID, new Specification(
                FAKE_IMAGE_FLAVOR_NAME,
                FAKE_USER_ID
        ), FAKE_UUID);
        List<Task> tasks = new ArrayList<>();
        tasks.add(task);

        ArrayList<JDFJob> jobs = new ArrayList<>();
        JDFJob jdfJob = new JDFJob(FAKE_USER_ID, tasks, null);
        jobs.add(jdfJob);

        doReturn(jobs).when(this.dataStore).getAllByUserId(FAKE_USER_ID);
        assert (jobs.get(0).equals(this.iguassuController.getAllJobs(FAKE_USER_ID).get(0)));
        Assert.assertEquals(task, this.iguassuController.getTaskById(FAKE_TASK_ID, FAKE_USER_ID));

        jdfJob.addTask(task);
        // jdfJob.run(task);
        tasks.add(task);

        doNothing().when(iguassuController).updateJob(any(JDFJob.class));
        // update DB Map

        Assert.assertEquals(jobs, this.iguassuController.getAllJobs(FAKE_USER_ID));
        Assert.assertEquals(task, this.iguassuController.getTaskById(FAKE_TASK_ID, FAKE_USER_ID));
    }

    @Test
    public void testTaskStateAfterControllerRestart() {
        Specification spec = new Specification(
                FAKE_IMAGE_FLAVOR_NAME,
                FAKE_USER_ID
        );
        List<String> taskIds = new ArrayList<>();
        JDFJob job = new JDFJob("testuser", new ArrayList<Task>(), "'this is a test user");
        Task task = new Task("TaskNumber-" + 0 + "-" + UUID.randomUUID(), spec, "0000");
        task.setState(TaskState.READY);
        taskIds.add(task.getId());
        job.addTask(task);

        task = new Task("TaskNumber-" + 1 + "-" + UUID.randomUUID(), spec, "0000");
        task.setState(TaskState.RUNNING);
        taskIds.add(task.getId());
        job.addTask(task);

        task = new Task("TaskNumber-" + 2 + "-" + UUID.randomUUID(), spec, "0000");
        task.setState(TaskState.FINISHED);
        taskIds.add(task.getId());
        job.addTask(task);

        task = new Task("TaskNumber-" + 5 + "-" + UUID.randomUUID(), spec, "0000");
        task.setState(TaskState.FAILED);
        taskIds.add(task.getId());
        job.addTask(task);

        this.dataStore.insert(job);
    }
}
