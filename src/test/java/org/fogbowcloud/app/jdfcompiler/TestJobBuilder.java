package org.fogbowcloud.app.jdfcompiler;

import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TestJobBuilder {

    private static final String RESOURCE_DIR = "test" + File.separator + "resources";
    private static final String SIMPLE_JOB_EXAMPLE = RESOURCE_DIR + File.separator + "SimpleJob2.jdf";
    private static final String FAKE_USER_NAME = "fake-username";
    private static final String FAKE_EXTERNAL_OAUTH_TOKEN = "fake-external-oauth-token";
    private static final Long FAKE_TOKEN_VERSION = 0L;

    @Test
    public void testJDFCompilation() throws CompilerException, IOException, InterruptedException {
//        Properties properties = new Properties();
//
//        User user = new DefaultUser("iguassuService", "iguassuService");
//
//        CommonCompiler commonCompiler = new CommonCompiler();
//        commonCompiler.compile(SIMPLE_JOB_EXAMPLE, FileType.JDF);
//        JobSpecification jobSpec = (JobSpecification) commonCompiler.getResult().get(0);
//
//        JobBuilder jobBuilder = new JobBuilder(properties);
//        jobBuilder.createJobFromJDFFile(
//                testJob,
//                SIMPLE_JOB_EXAMPLE,
//                jobSpec,
//                FAKE_USER_NAME,
//                FAKE_EXTERNAL_OAUTH_TOKEN,
//                FAKE_TOKEN_VERSION
//        );
//        List<Task> tasks = testJob.getTasksAsList();
//
//        assertEquals(tasks.size(), 3);
//        for (Command command : tasks.get(0).getAllCommands()) {
//            System.out.println(command.getCommand());
//        }
    }
}
