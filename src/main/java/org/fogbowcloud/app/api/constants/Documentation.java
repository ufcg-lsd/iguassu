package org.fogbowcloud.app.api.constants;

public class Documentation {

	public static class Endpoint {

		public static final String AUTH = "auth";
		public static final String VERSION = "version";
		public static final String NONCE = "nonce";
		public static final String JOB = "job";
		public static final String JOB_ID = "/{jobId}";
		public static final String TASK = "/task";
		public static final String STATUS = "/status";
		public static final String OAUTH_2 = "/oauth2";
		public static final String REFRESH_TOKEN_VERSION = OAUTH_2 + "/refresh/{userId}/{tokenVersion}";
	}

	public static class ApiInfo {

		public static final String TITLE = "Iguassu";
		public static final String DESCRIPTION =
			"Iguassu is a tool for monitoring and executing jobs in "
				+ "a multi-cloud environment federated by the fogbow middleware. "
				+ "Iguassu allows the user to harness cloud resources without bothering about the "
				+ "details of the cloud infrastructure.";
		public static final String CONTACT_NAME = "Iguassu";
		public static final String CONTACT_URL = "";
		public static final String CONTACT_EMAIL = "";
	}

	public static class Job {

		public static final String DESCRIPTION = "Manages jobs.";
		public static final String CREATE_OPERATION = "Creates an job.";
		public static final String GET_ALL_OPERATION = "Lists all jobs.";
		public static final String GET_BY_ID_OPERATION = "Get a specific job.";
		public static final String DELETE_OPERATION = "Deletes a specific job.";
		public static final String GET_TASKS_OPERATION = "Get tasks of a specific job.";

		public static final String ID = "The ID of the specific job.";
		public static final String CREATE_REQUEST_PARAM = "Request parameter of URL";
	}

	public static class Nonce {

		public static final String DESCRIPTION = "Manages nonces";
		public static final String GET_OPERATION = "Creates a nonce";
	}

	public static class Version {

		public static final String DESCRIPTION = "Describes the current version of the Iguassu API.";
		public static final String GET_OPERATION = "Returns the current version.";
	}

	public static class Auth {

		public static final String DESCRIPTION = "Manages authentication and authorization";
		public static final String AUTHENTICATE_USER =
			"Authenticate an user returning an Iguassu Token " + "from OAuth2 Authorization Code";
		public static final String AUTHORIZATION_CODE =
			"The body of the request must specify a valid OAuth2" + "Authorization Code";
	}

	public static class CommonParameters {

		public static final String USER_CREDENTIALS =
			"The header of the request must specify a valid Iguassu Token " + "and User Identifier";
		public static final String OAUTH_CREDENTIALS =
			"The header of the request must contain the right client id, " + "secret and redirect uri";
	}
}
