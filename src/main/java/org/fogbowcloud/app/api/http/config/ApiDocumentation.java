package org.fogbowcloud.app.api.http.config;

public class ApiDocumentation {
	public static class ApiInfo {
		public static final String API_TITLE = "Iguassu";
		public static final String API_DESCRIPTION = "";

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
		public static final String CREATE_REQUEST_PARAM = "";
	}

	public static class Nonce {
		public static final String API = "";
		public static final String GET_OPERATION = "";
	}

	public static class OAuthToken {
		public static final String API = "";
		public static final String STORE_OPERATION = "Store an token.";
		public static final String GET_OPERATION = "Lists all jobs.";
		public static final String DELETE_OPERATION = "Deletes all tokens.";
		public static final String CREATE_REQUEST_BODY = "";
		public static final String GET_BY_USER = "Gets the token of user";

		public static final String USER_NAME = "The name of the specific user";
	}

	public static class CommonParameters {
		public static final String CREDENTIALS = "";
	}
}
