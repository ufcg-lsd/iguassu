package org.fogbowcloud.app.core;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.*;

import org.fogbowcloud.app.core.constants.IguassuGeneralConstants;
import org.fogbowcloud.app.core.datastore.JobDataStore;
import org.fogbowcloud.app.core.task.Specification;
import org.fogbowcloud.app.core.task.Task;
import org.fogbowcloud.app.core.task.TaskImpl;
import org.fogbowcloud.app.core.task.TaskState;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.core.authenticator.models.LDAPUser;
import org.fogbowcloud.app.core.authenticator.models.User;
import org.fogbowcloud.app.core.constants.IguassuPropertiesConstants;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TestIguassuController {

    private static final String FAKE_UUID = "1234";
    private static final String FAKE_CLOUD_NAME = "cloudfake";
    private static final String FAKE_OWNER = "testowner";
    private static final String FAKE_TASK_ID = "testtaskId";
    private static final String FAKE_IMAGE_FLAVOR_NAME = "testimage";
    private static final String FAKE_PUBLIC_KEY = "testPublicKey";
    private static final String FAKE_PRIVATE_KEY_FILE_PATH = "testPrivateKeyPath";

    private IguassuController iguassuController;
    private JobDataStore dataStore;

    @Before
    public void setUp() throws Exception {
        Properties properties = new Properties();
        properties.put(IguassuPropertiesConstants.REST_SERVER_PORT, "4444");
        properties.put(IguassuPropertiesConstants.EXECUTION_MONITOR_PERIOD, "60000");
        properties.put(
                IguassuPropertiesConstants.AUTHENTICATION_PLUGIN,
                "org.fogbowcloud.app.utils.authenticator.CommonAuthenticator"
        );
        properties.put(IguassuPropertiesConstants.IGUASSU_PUBLIC_KEY, FAKE_PUBLIC_KEY);
        properties.put(IguassuPropertiesConstants.IGUASSU_PRIVATE_KEY_FILEPATH, FAKE_PRIVATE_KEY_FILE_PATH);

        properties.put(IguassuPropertiesConstants.REMOTE_OUTPUT_FOLDER, "/tmp");
        properties.put(IguassuPropertiesConstants.LOCAL_OUTPUT_FOLDER, "/tmp");

        properties.put(IguassuPropertiesConstants.INFRA_RESOURCE_CONNECTION_TIMEOUT, "300000000");
        properties.put(IguassuPropertiesConstants.INFRA_RESOURCE_IDLE_LIFETIME, "30000");
        properties.put(IguassuPropertiesConstants.INFRA_IS_ELASTIC, "true");
        properties.put(
                IguassuPropertiesConstants.TOKEN_UPDATE_PLUGIN,
                "org.fogbowcloud.blowout.infrastructure.token.LDAPTokenUpdatePlugin"
        );
        properties.put(
                IguassuPropertiesConstants.INFRA_PROVIDER_PLUGIN,
                "org.fogbowcloud.blowout.infrastructure.provider.fogbow.FogbowInfrastructureProvider"
        );
        properties.put(IguassuGeneralConstants.DB_DATASTORE_URL, "jdbc:h2:/tmp/datastores/testfogbowresourcesdatastore");
        this.iguassuController = Mockito.spy(new IguassuController(properties));
        this.dataStore = Mockito.spy(new JobDataStore(properties.getProperty(IguassuGeneralConstants.DB_DATASTORE_URL)));

        this.iguassuController.setDataStore(dataStore);
    }

    @After
    public void tearDown() {
        this.dataStore.deleteAll();
    }

    @Test
    public void testRestart() throws JSONException {
        ArrayList<Task> taskList = new ArrayList<>();
        Specification spec = new Specification(
                FAKE_CLOUD_NAME,
                FAKE_IMAGE_FLAVOR_NAME,
                FAKE_OWNER,
                FAKE_PUBLIC_KEY,
                FAKE_PRIVATE_KEY_FILE_PATH,
                "",
                ""
        );
        Task task = new TaskImpl(FAKE_TASK_ID, spec, FAKE_UUID);
        taskList.add(task);

        JDFJob job = new JDFJob(FAKE_OWNER, taskList, "");
        job.finishCreation();
        this.iguassuController.getJobDataStore().insert(job);

        this.iguassuController.restartAllJobs();

        Assert.assertEquals(1, this.iguassuController.getAllJobs(FAKE_OWNER).size());
        JDFJob job1 = this.iguassuController.getAllJobs(FAKE_OWNER).get(0);
        assert (job1.equals(job));
        System.out.println(this.iguassuController.getAllJobs(FAKE_OWNER).get(0).getTaskById(FAKE_TASK_ID).getSpecification().toJSON().toString());

        Specification spec2 = Specification.fromJSON(
                new JSONObject(
                        this.iguassuController
                                .getAllJobs(FAKE_OWNER)
                                .get(0)
                                .getTaskById(FAKE_TASK_ID)
                                .getSpecification()
                                .toJSON()
                                .toString()
                )
        );
        Assert.assertEquals(1, this.iguassuController.getAllJobs(FAKE_OWNER).get(0).getTaskList().size());
        assert (spec.equals(spec2));
        assert (task.equals(this.iguassuController.getAllJobs(FAKE_OWNER).get(0).getTaskList().get(FAKE_TASK_ID)));
        assert (spec.equals(this.iguassuController.getAllJobs(FAKE_OWNER).get(0).getTaskList().get(FAKE_TASK_ID).getSpecification()));

    }

    @Test
    public void testGetJobById() {
        String jobId = "jobId00";
        JDFJob job = new JDFJob(jobId, FAKE_OWNER, new ArrayList<Task>(), null);
        doReturn(job).when(dataStore).getByJobId(jobId, FAKE_OWNER);
        assert (job.equals(this.iguassuController.getJobById(jobId, FAKE_OWNER)));

    }

    @Test
    public void testAddJob() throws Exception {
        String jdfFilePath = "";
        User user = new LDAPUser("testuser", "'this is a test user'");

        JDFJob job = new JDFJob(user.getUser(), new ArrayList<Task>(), user.getUsername());
        Mockito.doReturn(job).when(this.iguassuController).runJobFromJDFFile(
                Mockito.anyString(),
                Mockito.any(User.class)
        );

        String id = this.iguassuController.addJob(jdfFilePath, user);
        Assert.assertEquals(id, job.getId());
        Mockito.verify(this.dataStore).insert(job);
        Assert.assertTrue(this.dataStore.getByJobId(id, user.getUser()).equals(job));
    }

    @Test
    public void testGetAllJobs() {
        ArrayList<JDFJob> jobs = new ArrayList<>();
        ArrayList<Task> task = new ArrayList<>();
        jobs.add(new JDFJob("job1", FAKE_OWNER, task, null));
        jobs.add(new JDFJob("job2", FAKE_OWNER, task, null));
        jobs.add(new JDFJob("job3", FAKE_OWNER, task, null));
        jobs.add(new JDFJob("job4", FAKE_OWNER, task, null));

        doReturn(jobs).when(this.dataStore).getAllByOwner(FAKE_OWNER);
        doNothing().when(this.iguassuController).updateJob(any(JDFJob.class));

        this.iguassuController.getAllJobs(FAKE_OWNER);

        Assert.assertEquals(jobs.size(), this.iguassuController.getAllJobs(FAKE_OWNER).size());
    }

    @Test
    public void testGetAllJobsWithoutAnotherUser() {
        ArrayList<JDFJob> jobs = new ArrayList<>();
        ArrayList<Task> task = new ArrayList<>();

        jobs.add(new JDFJob(FAKE_OWNER, task, null));
        jobs.add(new JDFJob(FAKE_OWNER, task, null));
        jobs.add(new JDFJob(FAKE_OWNER, task, null));
        jobs.add(new JDFJob(FAKE_OWNER, task, null));
        doReturn(jobs).when(this.dataStore).getAllByOwner(FAKE_OWNER);

        Assert.assertEquals(0, this.iguassuController.getAllJobs("wrong user owner").size());

        jobs.add(new JDFJob(FAKE_OWNER, task, null));
        jobs.add(new JDFJob(FAKE_OWNER, task, null));
        jobs.add(new JDFJob(FAKE_OWNER, task, null));
        jobs.add(new JDFJob(FAKE_OWNER, task, null));

        Assert.assertEquals(0, this.iguassuController.getAllJobs("wrong user owner").size());

        JobDataStore jobDataStore = this.iguassuController.getJobDataStore();
        Assert.assertEquals(0, jobDataStore.getAllByOwner("wrong user owner").size());
    }

    @Test
    public void testGetJobByName() {
        String jobName = "jobName00";
        ArrayList<JDFJob> jobs = new ArrayList<>();
        ArrayList<Task> task = new ArrayList<>();
        JDFJob jdfJob = new JDFJob(FAKE_OWNER, task, null);
        jdfJob.setFriendlyName(jobName);
        jobs.add(jdfJob);
        jobs.add(new JDFJob(FAKE_OWNER, task, null));
        jobs.add(new JDFJob(FAKE_OWNER, task, null));

        jobs.add(new JDFJob(FAKE_OWNER, task, null));
        jobs.add(new JDFJob(FAKE_OWNER, task, null));

        doReturn(jobs).when(this.dataStore).getAllByOwner(FAKE_OWNER);

        this.iguassuController.getJobByName(jobName, FAKE_OWNER);
        assert (jdfJob.equals(this.iguassuController.getJobByName(jobName, FAKE_OWNER)));
    }

    @Test
    public void testStopJob() {
        String jobName = "jobName00";

        JDFJob jdfJob = new JDFJob(FAKE_OWNER, new ArrayList<Task>(), null);
        jdfJob.setFriendlyName(jobName);
        doReturn(true).when(this.dataStore).deleteByJobId(jdfJob.getId(), FAKE_OWNER);
        doNothing().when(iguassuController).updateJob(any(JDFJob.class));
        doReturn(jdfJob).when(iguassuController).getJobByName(jobName, FAKE_OWNER);
        // update DB Map
        this.iguassuController.stopJob(jobName, FAKE_OWNER);

        Mockito.verify(this.dataStore).deleteByJobId(jdfJob.getId(), FAKE_OWNER);
    }

    @Test
    public void testStopJobWithId() {
        ArrayList<JDFJob> jobs = new ArrayList<>();
        ArrayList<Task> task = new ArrayList<>();

        JDFJob jdfJob = new JDFJob(FAKE_OWNER, task, null);
        jobs.add(jdfJob);
        jobs.add(new JDFJob("job1", FAKE_OWNER, task, null));
        jobs.add(new JDFJob("job2", FAKE_OWNER, task, null));

        doReturn(jobs).when(this.dataStore).getAllByOwner(FAKE_OWNER);
        doReturn(true).when(this.dataStore).deleteByJobId(jdfJob.getId(), FAKE_OWNER);
        doNothing().when(iguassuController).updateJob(any(JDFJob.class));
        doReturn(jdfJob).when(this.dataStore).getByJobId(jdfJob.getId(), FAKE_OWNER);
        // update DB Map
        this.iguassuController.stopJob(jdfJob.getId(), FAKE_OWNER);

        Mockito.verify(this.dataStore).deleteByJobId(jdfJob.getId(), FAKE_OWNER);
    }

    @Test
    public void testGetTaskById() {

        Task task = new TaskImpl(FAKE_TASK_ID, new Specification(
                FAKE_CLOUD_NAME,
                FAKE_IMAGE_FLAVOR_NAME,
                FAKE_OWNER,
                FAKE_PUBLIC_KEY,
                FAKE_PRIVATE_KEY_FILE_PATH,
                "",
                ""
        ), FAKE_UUID);
        List<Task> tasks = new ArrayList<>();
        tasks.add(task);

        ArrayList<JDFJob> jobs = new ArrayList<>();
        JDFJob jdfJob = new JDFJob(FAKE_OWNER, tasks, null);
        jobs.add(jdfJob);

        doReturn(jobs).when(this.dataStore).getAllByOwner(FAKE_OWNER);
        assert (jobs.get(0).equals(this.iguassuController.getAllJobs(FAKE_OWNER).get(0)));
        Assert.assertEquals(task, this.iguassuController.getTaskById(FAKE_TASK_ID, FAKE_OWNER));

        jdfJob.addTask(task);
        // jdfJob.run(task);
        tasks.add(task);

        doNothing().when(iguassuController).updateJob(any(JDFJob.class));
        // update DB Map

        Assert.assertEquals(jobs, this.iguassuController.getAllJobs(FAKE_OWNER));
        Assert.assertEquals(task, this.iguassuController.getTaskById(FAKE_TASK_ID, FAKE_OWNER));
    }

    @Test
    public void testTaskStateAfterControllerRestart() {
        Specification spec = new Specification(
                FAKE_CLOUD_NAME,
                FAKE_IMAGE_FLAVOR_NAME,
                FAKE_OWNER,
                FAKE_PUBLIC_KEY,
                FAKE_PRIVATE_KEY_FILE_PATH,
                "",
                ""
        );
        List<String> taskIds = new ArrayList<>();
        JDFJob job = new JDFJob("testuser", new ArrayList<Task>(), "'this is a test user");
        Task task = new TaskImpl("TaskNumber-" + 0 + "-" + UUID.randomUUID(), spec, "0000");
        task.setState(TaskState.READY);
        taskIds.add(task.getId());
        job.addTask(task);

        task = new TaskImpl("TaskNumber-" + 1 + "-" + UUID.randomUUID(), spec, "0000");
        task.setState(TaskState.RUNNING);
        taskIds.add(task.getId());
        job.addTask(task);

        task = new TaskImpl("TaskNumber-" + 2 + "-" + UUID.randomUUID(), spec, "0000");
        task.setState(TaskState.FINISHED);
        taskIds.add(task.getId());
        task.finish();
        job.addTask(task);

        task = new TaskImpl("TaskNumber-" + 3 + "-" + UUID.randomUUID(), spec, "0000");
        task.setState(TaskState.COMPLETED);
        taskIds.add(task.getId());
        task.finish();
        job.addTask(task);

        task = new TaskImpl("TaskNumber-" + 4 + "-" + UUID.randomUUID(), spec, "0000");
        task.setState(TaskState.NOT_CREATED);
        taskIds.add(task.getId());
        job.addTask(task);

        task = new TaskImpl("TaskNumber-" + 5 + "-" + UUID.randomUUID(), spec, "0000");
        task.setState(TaskState.FAILED);
        taskIds.add(task.getId());
        job.addTask(task);

        task = new TaskImpl("TaskNumber-" + 6 + "-" + UUID.randomUUID(), spec, "0000");
        task.setState(TaskState.TIMEDOUT);
        taskIds.add(task.getId());
        job.addTask(task);

        this.dataStore.insert(job);
        this.iguassuController.restartAllJobs();

        System.out.println(taskIds.get(0));
        Assert.assertEquals(TaskState.READY, this.iguassuController.getTaskState(taskIds.get(0)));
        System.out.println(taskIds.get(1));
        Assert.assertEquals(TaskState.READY, this.iguassuController.getTaskState(taskIds.get(1)));
        System.out.println(taskIds.get(2));
        Assert.assertEquals(TaskState.COMPLETED, this.iguassuController.getTaskState(taskIds.get(2)));
        System.out.println(taskIds.get(3));
        Assert.assertEquals(TaskState.COMPLETED, this.iguassuController.getTaskState(taskIds.get(3)));
        System.out.println(taskIds.get(4));
        Assert.assertEquals(TaskState.READY, this.iguassuController.getTaskState(taskIds.get(4)));
        System.out.println(taskIds.get(5));
        Assert.assertEquals(TaskState.READY, this.iguassuController.getTaskState(taskIds.get(5)));
        System.out.println(taskIds.get(6));
        Assert.assertEquals(TaskState.READY, this.iguassuController.getTaskState(taskIds.get(6)));
    }
}
