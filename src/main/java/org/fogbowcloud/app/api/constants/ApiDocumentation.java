package org.fogbowcloud.app.api.constants;

public class ApiDocumentation {
	public static class ApiEndpoints {
		public static final String OAUTH_TOKEN_ENDPOINT = "oauthtoken";
		public static final String NONCE_ENDPOINT = "nonce";
		public static final String JOB_ENDPOINT = "job";
		public static final String JOB_PATH = "/{jobId}";
		public static final String TASKS_ENDPOINT = "tasks";
	}

	public static class ApiInfo {
		public static final String API_TITLE = "Iguassu";
		public static final String API_DESCRIPTION = "Iguassu is a tool for monitoring and executing jobs in " +
				"a multi-cloud environment federated by the fogbow middleware. " +
				"Iguassu allows the user to harness cloud resources without bothering about the details of the cloud infrastructure.";;

		public static final String CONTACT_NAME = "Iguassu";
		public static final String CONTACT_URL = "";
		public static final String CONTACT_EMAIL = "";
	}

	public static class Job {
		public static final String API = "Manages jobs.";
		public static final String CREATE_OPERATION = "Creates an job.";
		public static final String GET_OPERATION = "Lists all jobs.";
		public static final String GET_BY_ID_OPERATION = "Get a specific job.";
		public static final String DELETE_OPERATION = "Deletes a specific job.";
		public static final String GET_TASKS_OPERATION = "Get tasks of a specific job.";

		public static final String ID = "The ID of the specific job.";
		public static final String CREATE_REQUEST_PARAM = "Request parameter of URL";
	}

	public static class Nonce {
		public static final String API = "Manages nonces";
		public static final String GET_OPERATION = "Creates a nonce";
	}

	public static class Auth {
		public static final String API = "Manages authentication and authorization";
		public static final String AUTHENTICATE_USER = "Authenticate an user returning an Iguassu token " +
				"from OAuth Authorization Code";
		public static final String DELETE_OPERATION = "Deletes all tokens.";
		public static final String GET_BY_USER = "Gets the token of user";

		public static final String USER_NAME = "The name of the specific user";
		public static final String REQUEST_ACCESS_TOKEN_BODY_MSG = "The body of the request must specify a valid " +
				"Authorization Code";
	}

	public static class CommonParameters {
		public static final String CREDENTIALS = "The header of the request";
		public static final String OAUTH_CREDENTIALS = "The header of the request must contain the right client id, " +
				"secret and redirect uri";

	}
}
