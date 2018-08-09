package org.fogbowcloud.app.resource;

import java.util.Properties;

import org.fogbowcloud.app.IguassuController;
import org.fogbowcloud.app.model.User;
import org.fogbowcloud.app.restlet.JDFSchedulerApplication;
import org.fogbowcloud.app.utils.ArrebolPropertiesConstants;
import org.mockito.Mockito;

public class ResourceTestUtil {
	public static final String WRONG_CRED = "wrong cred";
	public static final String DEFAULT_SERVER_PORT = "30023";
	public static final String DEFAULT_PREFIX_URL = "http://localhost:" + DEFAULT_SERVER_PORT;
	public static final String DEFAULT_OWNER = "arrebolservice";
	

	public static final String JOB_RESOURCE_SUFIX = "/arrebol/job";
	public static final String JOB_ENDPOINT_SUFIX = "/arrebol/job/ui";
	public static final String TASK_RESOURCE_SUFIX = "/arrebol/task/";

	private JDFSchedulerApplication jdfSchedulerApplication;
	private IguassuController iguassuController;

	public ResourceTestUtil() throws Exception {

		this.iguassuController = Mockito.mock(IguassuController.class);
		Mockito.doNothing().when(this.iguassuController).init();

		Properties properties = new Properties();
		properties.put(ArrebolPropertiesConstants.REST_SERVER_PORT, DEFAULT_SERVER_PORT);
		Mockito.when(this.iguassuController.getProperties()).thenReturn(properties);
		User userMock = Mockito.mock(User.class);
		
		Mockito.doReturn(ResourceTestUtil.DEFAULT_OWNER).when(userMock).getUser();

		Mockito.when(this.iguassuController.authUser(null)).thenReturn(userMock);
		
		Mockito.when(this.iguassuController.authUser(WRONG_CRED)).thenReturn(null);
		
		this.jdfSchedulerApplication = new JDFSchedulerApplication(this.iguassuController);
	}

	public JDFSchedulerApplication getJdfSchedulerApplication() {
		return jdfSchedulerApplication;
	}

	public IguassuController getIguassuController() {
		return iguassuController;
	}

}
