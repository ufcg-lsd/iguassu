package org.fogbowcloud.app.jdfcompiler;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.fogbowcloud.app.core.authenticator.models.User;
import org.fogbowcloud.app.core.authenticator.models.UserImpl;
import org.fogbowcloud.app.core.command.Command;
import org.fogbowcloud.app.core.task.Task;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.job.JDFJobBuilder;
import org.fogbowcloud.app.jdfcompiler.job.JobSpecification;
import org.fogbowcloud.app.jdfcompiler.main.CommonCompiler;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.app.jdfcompiler.main.CommonCompiler.FileType;
import org.fogbowcloud.app.core.constants.IguassuPropertiesConstants;
import org.junit.Test;

public class TestJDFJobBuilder {

	private static final String RESOURCE_DIR = "test" + File.separator + "resources";
	private static final String SIMPLE_JOB_EXAMPLE = RESOURCE_DIR + File.separator + "SimpleJob2.jdf";
	private static final String FAKE_USER_NAME = "fake-username";
	private static final String FAKE_EXTERNAL_OAUTH_TOKEN = "fake-external-oauth-token";

	@Test
	public void testJDFCompilation () throws CompilerException, IOException, InterruptedException {
		Properties properties = new Properties();

		User owner = new UserImpl("iguassuService", "iguassuService");
		JDFJob testJob = new JDFJob(owner.getUserIdentification(), new ArrayList<>(), owner.getUserIdentification());
		CommonCompiler commonCompiler = new CommonCompiler();
		commonCompiler.compile(SIMPLE_JOB_EXAMPLE, FileType.JDF);
		JobSpecification jobSpec = (JobSpecification) commonCompiler.getResult().get(0);

		JDFJobBuilder jdfJobBuilder = new JDFJobBuilder(properties);
		jdfJobBuilder.createJobFromJDFFile(
				testJob,
				SIMPLE_JOB_EXAMPLE,
				jobSpec,
				FAKE_USER_NAME,
				FAKE_EXTERNAL_OAUTH_TOKEN
		);
		List<Task> tasks = testJob.getTasks();
		
		assertEquals(tasks.size(), 3);
		for (Command command : tasks.get(0).getAllCommands()) {
			System.out.println(command.getCommand());
		}
	}
}
