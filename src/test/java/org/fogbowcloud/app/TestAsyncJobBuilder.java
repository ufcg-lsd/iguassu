package org.fogbowcloud.app;

import org.fogbowcloud.app.core.IguassuController;
import org.fogbowcloud.app.core.datastore.JobDataStore;
import org.fogbowcloud.app.core.exceptions.IguassuException;
import org.fogbowcloud.app.core.exceptions.NameAlreadyInUseException;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.core.authenticator.models.LDAPUser;
import org.fogbowcloud.blowout.core.BlowoutController;
import org.fogbowcloud.blowout.core.exception.BlowoutException;
import org.fogbowcloud.blowout.core.model.task.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class TestAsyncJobBuilder {

    private static final String SIMPLE_JOB_EXAMPLE = "test" + File.separator + "resources" + File.separator + "SimpleJob2.jdf";
    private static final String IGUASSU_CONF = "iguassu.conf";
    private static final String BLOWOUT_CONF = "sched.conf";

    private static final String user = "iguassuService";
    private static final String username = "iguassuservice";

    private IguassuController iguassuController;
    private BlowoutController blowout;


    @Before
    public void setUp() throws Exception {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(IGUASSU_CONF));
            properties.load(new FileInputStream(BLOWOUT_CONF));
            iguassuController = Mockito.spy(new IguassuController(properties));
        } catch (BlowoutException | IguassuException | IOException e) {
            e.printStackTrace();
        }

        JobDataStore dataStore = Mockito.spy(new JobDataStore("jdbc:h2:/tmp/datastores/testfogbowresourcesdatastore"));
        iguassuController.setDataStore(dataStore);

        blowout = Mockito.mock(BlowoutController.class);
        iguassuController.setBlowoutController(blowout);

        iguassuController.init();
    }

    @Test
    public void testAsyncJobCreation() {
        try {
            Mockito.doNothing().when(blowout).addTaskList(Mockito.anyListOf(Task.class));

            String id = iguassuController.addJob(SIMPLE_JOB_EXAMPLE, new LDAPUser(user, username));

            JDFJob job = iguassuController.getJobById(id, user);
            Assert.assertEquals(JDFJob.JDFJobState.SUBMITTED, job.getState());
            Assert.assertEquals(0, job.getTasks().size());

            iguassuController.waitForJobCreation(job.getId());

            job = iguassuController.getJobById(id, user);
            Assert.assertEquals(JDFJob.JDFJobState.CREATED, job.getState());
            Assert.assertEquals(3, job.getTasks().size());
        } catch (CompilerException | NameAlreadyInUseException | BlowoutException | IOException | InterruptedException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testDeleteJobWhileBeingCreated() {
        try {
            Mockito.doNothing().when(blowout).addTaskList(Mockito.anyListOf(Task.class));

            String id = iguassuController.addJob(SIMPLE_JOB_EXAMPLE, new LDAPUser(user, username));

            JDFJob job = iguassuController.getJobById(id, user);
            Assert.assertEquals(JDFJob.JDFJobState.SUBMITTED, job.getState());
            Assert.assertEquals(0, job.getTasks().size());

            iguassuController.stopJob(id, user);

            job = iguassuController.getJobById(id, user);
            Assert.assertNull(job);
        } catch (CompilerException | NameAlreadyInUseException | BlowoutException | IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
