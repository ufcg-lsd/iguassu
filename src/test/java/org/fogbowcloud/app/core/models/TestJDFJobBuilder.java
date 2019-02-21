package org.fogbowcloud.app.core.models;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.fogbowcloud.app.jdfcompiler.job.JobSpecification;
import org.fogbowcloud.app.jdfcompiler.main.CommonCompiler;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.app.jdfcompiler.main.CommonCompiler.FileType;
import org.fogbowcloud.app.core.constants.IguassuPropertiesConstants;
import org.fogbowcloud.blowout.core.model.Command;
import org.fogbowcloud.blowout.core.model.task.Task;
import org.junit.Test;

public class TestJDFJobBuilder {
	
	public static final String RESOURCE_DIR = "test" + File.separator + "resources";

	public static final String SIMPLE_JOB_EXAMPLE = RESOURCE_DIR + File.separator + "SimpleJob2.jdf";

	public static final String FAKE_USER_NAME = "fake-username";

	public static final String FAKE_EXTERNAL_OAUTH_TOKEN = "fake-external-oauth-token";

	@Test
	public void testJDFCompilation () throws CompilerException, IOException, InterruptedException {
		Properties properties = new Properties();
		properties.setProperty(IguassuPropertiesConstants.INFRA_PROVIDER_USERNAME, "some_infra_name");
		properties.setProperty(IguassuPropertiesConstants.IGUASSU_PUBLIC_KEY, "public_key");
		properties.setProperty(IguassuPropertiesConstants.IGUASSU_PRIVATE_KEY_FILEPATH, "file path");
		User owner = new LDAPUser("iguassuService", "iguassuService");
		JDFJob testJob = new JDFJob(owner.getUser(), new ArrayList<>(), owner.getUsername());
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
