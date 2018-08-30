package org.fogbowcloud.app;

import org.fogbowcloud.app.datastore.JobDataStore;
import org.fogbowcloud.app.exception.IguassuException;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.app.model.JDFJob;
import org.fogbowcloud.app.model.LDAPUser;
import org.fogbowcloud.blowout.core.BlowoutController;
import org.fogbowcloud.blowout.core.exception.BlowoutException;
import org.fogbowcloud.blowout.core.model.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class TestAsyncJobBuilder {

    private static final String EXSIMPLE_JOB = "test" + File.separator + "resources" + File.separator + "SimpleJob2.jdf";
    private static final String ARREBOL_CONF = "test" + File.separator + "arrebol.conf";
    private static final String BLOWOUT_CONF = "test" + File.separator + "blowout.conf";

    private static final String user = "arrebolservice";
    private static final String username = "arrebolservice";

    private IguassuController arrebol;
    private BlowoutController blowout;

    @Before
    public void setUp() {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(ARREBOL_CONF));
            properties.load(new FileInputStream(BLOWOUT_CONF));
            arrebol = Mockito.spy(new IguassuController(properties));
        } catch (BlowoutException | IguassuException | IOException e) {
            e.printStackTrace();
        }

        JobDataStore dataStore = Mockito.spy(new JobDataStore("jdbc:h2:/tmp/datastores/testfogbowresourcesdatastore"));
        arrebol.setDataStore(dataStore);

        blowout = Mockito.mock(BlowoutController.class);
        arrebol.setBlowoutController(blowout);
    }

    @Test
    public void testAsyncJobCreation() {
        try {
            Mockito.doNothing().when(blowout).addTaskList(Mockito.anyListOf(Task.class));

            String id = arrebol.addJob(EXSIMPLE_JOB, new LDAPUser(user, username));

            JDFJob job = arrebol.getJobById(id, user);
            Assert.assertEquals(JDFJob.JDFJobState.SUBMITTED, job.getState());
            Assert.assertEquals(0, job.getTasks().size());

            arrebol.waitForJobCreation(job.getId());

            job = arrebol.getJobById(id, user);
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

            String id = arrebol.addJob(EXSIMPLE_JOB, new LDAPUser(user, username));

            JDFJob job = arrebol.getJobById(id, user);
            Assert.assertEquals(JDFJob.JDFJobState.SUBMITTED, job.getState());
            Assert.assertEquals(0, job.getTasks().size());

            arrebol.stopJob(id, user);

            job = arrebol.getJobById(id, user);
            Assert.assertNull(job);
        } catch (CompilerException | NameAlreadyInUseException | BlowoutException | IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
