package org.fogbowcloud.app.jdfcompiler;

import static org.junit.Assert.assertEquals;
import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.fogbowcloud.app.jdfcompiler.job.JobSpecification;
import org.fogbowcloud.app.jdfcompiler.main.DescriptionFileCompile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestJobSpec {
	private static final String RESOURCE_DIR = "test" + File.separator + "resources";
	private static final String SIMPLE_JOB = RESOURCE_DIR + File.separator + "simplejob.jdf";
	private static final String SIMPLE_JOB_REQ = RESOURCE_DIR + File.separator + "simplejobreq.jdf";
	private static final String SIMPLEST_JOB = RESOURCE_DIR + File.separator + "simplestjob.jdf";
	private static final String SIMPLE_JOB_2 = RESOURCE_DIR + File.separator + File.separator + "simplejob2.jdf";
	private static final String SIMPLE_JOB_3 = RESOURCE_DIR + File.separator + File.separator + "simplejob3.jdf";
	private static final String SIMPLE_JOB_4 = RESOURCE_DIR + File.separator + File.separator + "NewSimpleJob.jdf";
	private static final String SIMPLE_JOB_5 = RESOURCE_DIR + File.separator + File.separator + "NewSimpleJob2.jdf";
	private static final String SIMPLE_JOB_6 = RESOURCE_DIR + File.separator + File.separator + "NewSimpleJob3.jdf";
	private static final String SIMPLE_JOB_7 = RESOURCE_DIR + File.separator + File.separator + "NewSimpleJob4.jdf";
	private static final String SIMPLE_JOB_8 = RESOURCE_DIR + File.separator + "FJob.jdf";
	private static final String SIMPLE_JOB1 = RESOURCE_DIR + File.separator + "SimpleJob1.jdf";
	private static final String SIMPLE_JOB2 = RESOURCE_DIR + File.separator + "SimpleJob2.jdf";
	private static final String SIMPLE_JOB3 = RESOURCE_DIR + File.separator + "SimpleJob3.jdf";
	private static final String SIMPLE_JOB4 = RESOURCE_DIR + File.separator + "SimpleJob4.jdf";
	private static final String JOB_WITH_LOCATION = RESOURCE_DIR + File.separator + "location.jdf";
	private static final String LOCATION_REQUIREMENT = " Glue2CloudComputeManagerID == \" SomeCloud \"";
	
	@Before
	public void setUp() {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();
	}

	@After
	public void tearDown() {
		System.gc();
		BasicConfigurator.resetConfiguration();
	}

	@Test
	public void testExSimpleJob() throws Exception {
		JobSpecification spec = DescriptionFileCompile.compileJDF(SIMPLE_JOB);
		assertEquals("SimpleJob", spec.getLabel());
		assertEquals(1, spec.getTaskSpecs().size());
	}

	@Test
	public void testSimplestJob() throws Exception {
		JobSpecification spec = DescriptionFileCompile.compileJDF(SIMPLEST_JOB);
		assertEquals("SimpleJob", spec.getLabel());
	}

	@Test
	public void testExSimpleJob2() throws Exception {
		JobSpecification spec = DescriptionFileCompile.compileJDF(SIMPLE_JOB_2);
		assertEquals("SimpleJob2", spec.getLabel());
		assertEquals(2, spec.getTaskSpecs().size());
	}

	@Test
	public void testExSimpleJob3() throws Exception {
		JobSpecification spec = DescriptionFileCompile.compileJDF(SIMPLE_JOB_3);
		assertEquals("SimpleJob3", spec.getLabel());
		assertEquals(1, spec.getTaskSpecs().size());
	}

	@Test
	public void testSimpleJob1() throws Exception {
		JobSpecification spec = DescriptionFileCompile.compileJDF(SIMPLE_JOB1);
		assertEquals("SimpleJob", spec.getLabel());
		assertEquals(4, spec.getTaskSpecs().size());
	}

	@Test
	public void testSimpleJob2() throws Exception {
		JobSpecification spec = DescriptionFileCompile.compileJDF(SIMPLE_JOB2);
		assertEquals("SimpleJob2", spec.getLabel());
		assertEquals(3, spec.getTaskSpecs().size());
	}

	@Test
	public void testSimpleJob3() throws Exception {
		JobSpecification spec = DescriptionFileCompile.compileJDF(SIMPLE_JOB3);
		assertEquals("SimpleJob", spec.getLabel());
		assertEquals(4, spec.getTaskSpecs().size());
	}

	@Test
	public void testSimpleJob4() throws Exception {
		JobSpecification spec = DescriptionFileCompile.compileJDF(SIMPLE_JOB4);
		assertEquals("SimpleJob4", spec.getLabel());
		assertEquals(1, spec.getTaskSpecs().size());
	}

	@Test
	public void testNewJobClausesMultiplePutOnInit() throws Exception {
		JobSpecification spec = DescriptionFileCompile.compileJDF(SIMPLE_JOB_4);
		assertEquals("SimpleJob4", spec.getLabel());
		assertEquals(4, spec.getTaskSpecs().size());
	}

	@Test
	public void testNewJobRunningThingsOnInit() throws Exception {
		JobSpecification spec = DescriptionFileCompile.compileJDF(SIMPLE_JOB_5);
		assertEquals("SimpleJob4", spec.getLabel());
		assertEquals(4, spec.getTaskSpecs().size());
	}

	@Test
	public void testNewJobRunningMoreThanOneLineOnRemote() throws Exception {
		JobSpecification spec = DescriptionFileCompile.compileJDF(SIMPLE_JOB_6);
		assertEquals("SimpleJob4", spec.getLabel());
		assertEquals(4, spec.getTaskSpecs().size());
	}

	@Test
	public void testNewJobRunningPutOnRemote() throws Exception {
		JobSpecification spec = DescriptionFileCompile.compileJDF(SIMPLE_JOB_7);
		assertEquals("SimpleJob4", spec.getLabel());
		spec.getTaskSpecs().get(0).getInitBlocks();
		assertEquals(4, spec.getTaskSpecs().size());
	}

	@Test
	public void testNewFJob() throws Exception {
		JobSpecification spec = DescriptionFileCompile.compileJDF(SIMPLE_JOB_8);
		assertEquals("SimpleJob4", spec.getLabel());
		spec.getTaskSpecs().get(0).getInitBlocks();
		assertEquals(3, spec.getTaskSpecs().size());
	}

	@Test
	public void testJobWithLocationClause() throws Exception {
		JobSpecification spec = DescriptionFileCompile.compileJDF(JOB_WITH_LOCATION);
		assertEquals("LocationJob", spec.getLabel());
		assertEquals(6, spec.getTaskSpecs().size());
		assertEquals(LOCATION_REQUIREMENT, spec.getRequirements());
	}

	@Test
	public void testExSimpleJobWithRequirements() throws Exception {
		JobSpecification spec = DescriptionFileCompile.compileJDF(SIMPLE_JOB_REQ);
		assertEquals("SimpleJobReq", spec.getLabel());
		assertEquals(1, spec.getTaskSpecs().size());
	}
}