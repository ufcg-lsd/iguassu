package org.fogbowcloud.app.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.fogbowcloud.app.NameAlreadyInUseException;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.app.model.JDFJob;
import org.fogbowcloud.app.model.User;
import org.fogbowcloud.app.restlet.JDFSchedulerApplication;
import org.fogbowcloud.app.utils.ArrebolPropertiesConstants;
import org.fogbowcloud.blowout.core.model.Task;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.restlet.data.MediaType;


public class TestJobResource {
	
	private ResourceTestUtil resourceTestUtil;
	
	@Before
	public void setUp() throws Exception {
		this.resourceTestUtil = new ResourceTestUtil();
		
		JDFSchedulerApplication jdfSchedulerApplication = resourceTestUtil.getJdfSchedulerApplication();
		jdfSchedulerApplication.startServer();
	}
	
	@After
	public void tearDown() throws Exception {
		resourceTestUtil.getJdfSchedulerApplication().stopServer();
	}
	
	@Test
	public void testGetJobNotFound() throws Exception {
		String jobId = "nof_found";
		HttpGet get = new HttpGet(ResourceTestUtil.DEFAULT_PREFIX_URL + ResourceTestUtil.JOB_RESOURCE_SUFIX + "/" + jobId);
		get.addHeader(new BasicHeader(ArrebolPropertiesConstants.X_AUTH_USER, ResourceTestUtil.DEFAULT_OWNER));
		
		HttpClient client = HttpClients.createMinimal();
		HttpResponse response = client.execute(get);
		
		Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
	}
	
	@Test
	public void testGetJobUnathorized() throws Exception {
		HttpGet get = new HttpGet(ResourceTestUtil.DEFAULT_PREFIX_URL + ResourceTestUtil.JOB_RESOURCE_SUFIX + "/" + "jobId");
		get.addHeader(new BasicHeader(ArrebolPropertiesConstants.X_AUTH_USER, "wrong owner"));
		get.addHeader(new BasicHeader(ArrebolPropertiesConstants.X_CREDENTIALS, ResourceTestUtil.WRONG_CRED));
		
		HttpClient client = HttpClients.createMinimal();
		HttpResponse response = client.execute(get);
		
		Assert.assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusLine().getStatusCode());
	}	
	
	@Test
	public void testSpecificGetJob() throws Exception {
		String jobName = "jobName00";
		String owner = ResourceTestUtil.DEFAULT_OWNER;
		HttpGet get = new HttpGet(ResourceTestUtil.DEFAULT_PREFIX_URL + ResourceTestUtil.JOB_RESOURCE_SUFIX + "/" + jobName);
		get.addHeader(new BasicHeader(ArrebolPropertiesConstants.X_AUTH_USER, owner));
		
		JDFJob job = new JDFJob("schedPath", owner, new ArrayList<Task>(), null);
		job.setFriendlyName(jobName);
		Mockito.when(resourceTestUtil.getArrebolController().getJobByName(Mockito.eq(jobName), Mockito.eq(owner))).thenReturn(job);
		
		HttpClient client = HttpClients.createMinimal();
		HttpResponse response = client.execute(get);
		String responseStr = EntityUtils.toString(response.getEntity(), "UTF-8");
		
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode()); 
		JSONObject jsonObject = new JSONObject(responseStr);
		Assert.assertEquals(job.getName(), jsonObject.optString("name"));
		Assert.assertEquals(job.getId(), jsonObject.optString("id"));
		Assert.assertEquals(0, jsonObject.optJSONArray("Tasks").length());
	}	
	
	@Test
	public void testGetJobs() throws Exception {
		HttpGet get = new HttpGet(ResourceTestUtil.DEFAULT_PREFIX_URL + ResourceTestUtil.JOB_RESOURCE_SUFIX);
		String owner = ResourceTestUtil.DEFAULT_OWNER;
		
		get.addHeader(new BasicHeader(ArrebolPropertiesConstants.X_AUTH_USER, owner));
		ArrayList<Task> taskList = new ArrayList<>();
		ArrayList<JDFJob> jobs = new ArrayList<>();
		jobs.add(new JDFJob("schedPath", owner, taskList, null));
		jobs.add(new JDFJob("schedPathTwo", owner, taskList, null));
		jobs.add(new JDFJob("schedPathThree", owner, taskList, null));
		Mockito.when(resourceTestUtil.getArrebolController().getAllJobs(Mockito.eq(owner))).thenReturn(jobs);
		
		HttpClient client = HttpClients.createMinimal();
		HttpResponse response = client.execute(get);
		String responseStr = EntityUtils.toString(response.getEntity(), "UTF-8");
		
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode()); 
		Assert.assertTrue(response.getEntity().getContentType().getValue()
				.contains(MediaType.TEXT_PLAIN.getName()));
		JSONArray jsonArrayObject = new JSONObject(responseStr).getJSONArray(JobResource.JOB_LIST);
		Assert.assertEquals(jobs.size(), jsonArrayObject.length());
	}	

	@Test
	public void testDeleteJob() throws Exception {
		String jobId = "jobId00";
		HttpDelete delete = new HttpDelete(ResourceTestUtil.DEFAULT_PREFIX_URL + 
				ResourceTestUtil.JOB_RESOURCE_SUFIX + "/" + jobId);	
		String owner = ResourceTestUtil.DEFAULT_OWNER;
		delete.addHeader(new BasicHeader(ArrebolPropertiesConstants.X_AUTH_USER, owner));
		
		Mockito.when(resourceTestUtil.getArrebolController().stopJob(Mockito.eq(jobId), Mockito.eq(owner))).thenReturn(jobId);
		
		HttpResponse response = HttpClients.createMinimal().execute(delete);		
		
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
		Assert.assertTrue(response.getEntity().getContentType().getValue()
				.contains(MediaType.TEXT_PLAIN.getName()));
	}
	
	@Test
	public void testDeleteJobNotFound() throws Exception {
		String jobId = "jobId00";
		HttpDelete delete = new HttpDelete(ResourceTestUtil.DEFAULT_PREFIX_URL + 
				ResourceTestUtil.JOB_RESOURCE_SUFIX + "/" + jobId);	
		delete.addHeader(new BasicHeader(ArrebolPropertiesConstants.X_AUTH_USER, "owner"));
		
		User userMock = Mockito.mock(User.class);
		
		Mockito.doReturn(ResourceTestUtil.DEFAULT_OWNER).when(userMock).getUser();
		
		Mockito.when(this.resourceTestUtil.getArrebolController().authUser( Mockito.anyString())).thenReturn(userMock);			
		
		HttpResponse response = HttpClients.createMinimal().execute(delete);
		
		Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusLine().getStatusCode());
	}
	
	@Test
	public void testPostJob() throws Exception {
		String owner = ResourceTestUtil.DEFAULT_OWNER;
		HttpPost post = new HttpPost(ResourceTestUtil.DEFAULT_PREFIX_URL + ResourceTestUtil.JOB_RESOURCE_SUFIX);
		post.addHeader(new BasicHeader(ArrebolPropertiesConstants.X_AUTH_USER, owner));
		
		
		String jobName = "jobName00";
		List<Task> taskList = new ArrayList<>();
		JDFJob job = new JDFJob("schedPath", owner, taskList, null);
		Mockito.when(resourceTestUtil.getArrebolController().getJobByName(Mockito.eq(jobName), Mockito.eq(owner))).thenReturn(job);
		String jdfFilePath = "jdfFilePath";
		String schedPath = "schedPath";
		String friendlyName = "friendly";
		String jobId = "jobId00";
		Mockito.when(resourceTestUtil.getArrebolController().addJob(Mockito.eq(jdfFilePath), Mockito.any(User.class))).thenReturn(jobId);
		
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody(JobResource.FRIENDLY, friendlyName, ContentType.TEXT_PLAIN);
		builder.addTextBody(JobResource.JDF_FILE_PATH, jdfFilePath, ContentType.TEXT_PLAIN);
		HttpEntity multipart = builder.build();		
		post.setEntity(multipart);
		
		HttpResponse response = HttpClients.createMinimal().execute(post);
		String responseStr = EntityUtils.toString(response.getEntity(), "UTF-8");
		
		Assert.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode()); 
		Assert.assertTrue(response.getEntity().getContentType().getValue()
				.contains(MediaType.TEXT_PLAIN.getName()));
		Assert.assertEquals(jobId, responseStr);
	}
	
	@Test
	public void testPostJobWithoutJdfFilePath() throws Exception {
		String owner = ResourceTestUtil.DEFAULT_PREFIX_URL;
		HttpPost post = new HttpPost(ResourceTestUtil.DEFAULT_PREFIX_URL + ResourceTestUtil.JOB_RESOURCE_SUFIX);
		post.addHeader(new BasicHeader(ArrebolPropertiesConstants.X_AUTH_USER, owner));
		
		String jobName = "jobName00";
		List<Task> taskList = new ArrayList<>();
		JDFJob job = new JDFJob(owner, taskList, null);
		Mockito.when(resourceTestUtil.getArrebolController().getJobByName(Mockito.eq(jobName), Mockito.eq(owner))).thenReturn(job);
		String jdfFilePath = "jdfFilePath";
		String friendlyName = "friendly";
		String jobId = "jobId00";
		Mockito.when(
				resourceTestUtil.getArrebolController().addJob(
						Mockito.eq(jdfFilePath),
						Mockito.any(User.class)
				)
		).thenReturn(jobId);
		
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody(JobResource.FRIENDLY, friendlyName, ContentType.TEXT_PLAIN);
		HttpEntity multipart = builder.build();		
		post.setEntity(multipart);
		
		HttpResponse response = HttpClients.createMinimal().execute(post);
		
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode()); 
	}	
	
	@Test
	public void testPostJobNotAcceptable() throws Exception {
		String owner = ResourceTestUtil.DEFAULT_OWNER;
		HttpPost post = new HttpPost(ResourceTestUtil.DEFAULT_PREFIX_URL + ResourceTestUtil.JOB_RESOURCE_SUFIX);
		post.addHeader(new BasicHeader(ArrebolPropertiesConstants.X_AUTH_USER, owner));
		
		List<Task> taskList = new ArrayList<>();
		JDFJob job = new JDFJob("", owner, taskList, null);
		job.setFriendlyName("friendlyName");
		Mockito.when(resourceTestUtil.getArrebolController().addJob(
				Mockito.anyString(),
				Mockito.any(User.class)
		)).thenThrow(new NameAlreadyInUseException("in user"));
		String jdfFilePath = "jdfFilePath";
		
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody(JobResource.FRIENDLY, "friendlyName", ContentType.TEXT_PLAIN);
		builder.addTextBody(JobResource.JDF_FILE_PATH, jdfFilePath, ContentType.TEXT_PLAIN);
		HttpEntity multipart = builder.build();		
		post.setEntity(multipart);
		
		HttpResponse response = HttpClients.createMinimal().execute(post);
		
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode()); 
	}	
	
	@Test
	public void testPostJobErrorWHenAddingJob() throws Exception {
		String owner = ResourceTestUtil.DEFAULT_OWNER;
		HttpPost post = new HttpPost(ResourceTestUtil.DEFAULT_PREFIX_URL + ResourceTestUtil.JOB_RESOURCE_SUFIX);
		post.addHeader(new BasicHeader(ArrebolPropertiesConstants.X_AUTH_USER, owner));
		
		String jobName = "jobName00";
		List<Task> taskList = new ArrayList<>();
		JDFJob job = new JDFJob(owner, taskList, null);
		Mockito.when(resourceTestUtil.getArrebolController().getJobByName(Mockito.eq(jobName), Mockito.anyString())).thenReturn(job);
		String jdfFilePath = "jdfFilePath";
		String friendlyName = "friendly";
		Mockito.when(resourceTestUtil.getArrebolController().addJob(
				Mockito.eq(jdfFilePath),
				Mockito.any(User.class))
		).thenThrow(new CompilerException(""));
		
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.addTextBody(JobResource.FRIENDLY, friendlyName, ContentType.TEXT_PLAIN);
		builder.addTextBody(JobResource.JDF_FILE_PATH, jdfFilePath, ContentType.TEXT_PLAIN);
		HttpEntity multipart = builder.build();		
		post.setEntity(multipart);
		
		HttpResponse response = HttpClients.createMinimal().execute(post);
		
		Assert.assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
	}	
	
	@Test
	public void testPostJobMediaTypeError() throws Exception {
		HttpPost post = new HttpPost(ResourceTestUtil.DEFAULT_PREFIX_URL + ResourceTestUtil.JOB_RESOURCE_SUFIX);
		List<NameValuePair> params = new ArrayList<>(2);
		params.add(new BasicNameValuePair("", ""));
		post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		
		HttpResponse response = HttpClients.createMinimal().execute(post);
		
		Assert.assertEquals(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE, response.getStatusLine().getStatusCode());
	}	
		
}
