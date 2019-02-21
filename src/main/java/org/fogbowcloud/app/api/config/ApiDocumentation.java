package org.fogbowcloud.app.api.config;

public class ApiDocumentation {
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
		public static final String GET_BY_ID_OPERATION = "Lists a specific job.";
		public static final String DELETE_OPERATION = "Deletes a specific job.";

		public static final String ID = "The ID of the specific job.";
		public static final String CREATE_REQUEST_PARAM = "Request parameter of URL";
	}

	public static class Nonce {
		public static final String API = "Manages nonces";
		public static final String GET_OPERATION = "Creates a nonce";
	}

	public static class OAuthToken {
		public static final String API = "Manages tokens";
		public static final String STORE_OPERATION = "Store an token.";
		public static final String GET_OPERATION = "Lists all jobs.";
		public static final String DELETE_OPERATION = "Deletes all tokens.";
		public static final String CREATE_REQUEST_BODY = "The body of the request must specify the OAuthToken";
		public static final String GET_BY_USER = "Gets the token of user";

		public static final String USER_NAME = "The name of the specific user";
	}

	public static class CommonParameters {
		public static final String CREDENTIALS = "The header of the request";
	}
}
