package org.fogbowcloud.app.api.constants;

/**
 * API swagger documentation constants.
 */
public class Documentation {

    public static class Endpoint {

        public static final String AUTH = "auth";
        public static final String OAUTH_2 = "/oauth2";
        public static final String REFRESH_TOKEN_VERSION =
                OAUTH_2 + "/refresh/{userId}/{tokenVersion}";

        public static final String VERSION = "version";
        public static final String NONCE = "nonce";
        public static final String QUEUES = "queues";
        public static final String PUBLIC_KEY = "publicKey";

        static final String JOBS = "/jobs";
        static final String TASKS = "/tasks";
        static final String QUEUE_ID = "/{queueId}";
        public static final String SUBMIT_JOB = QUEUE_ID + JOBS;
        public static final String RETRIEVE_ALL_JOBS = QUEUE_ID + JOBS;
        public static final String QUEUE = QUEUE_ID;
        static final String JOB_ID = "/{jobId}";
        public static final String RETRIEVE_JOB_BY_ID = QUEUE_ID + JOBS + JOB_ID;
        public static final String RETRIEVE_TASKS_BY_JOB = QUEUE_ID + JOBS + JOB_ID + TASKS;
        public static final String DELETE_JOB_BY_ID = QUEUE_ID + JOBS + JOB_ID;
        static final String NODES = "/nodes";
        public static final String NODE_ENDPOINT = QUEUE_ID + NODES;
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

    public static class Queue {
        public static final String TAG = "Queue API";
        public static final String DESCRIPTION = "Manages queues";
        public static final String SUBMIT_JOB = "Compiles and submits a job to execution";
        public static final String RETRIEVE_ALL_JOBS = "Retrieves the current state of all jobs of a given queue by user";
        public static final String RETRIEVE_JOB_BY_ID = "Retrieves a specific job";
        public static final String DELETE_JOB_BY_ID = "Deletes a specific job by id";
        public static final String RETRIEVE_TASKS_BY_JOB = "Get tasks of a specific job";
        public static final String CREATE_QUEUE = "Create a new queue";
        public static final String RETRIEVES_QUEUES = "Retrieves all queues";
        public static final String SUBMIT_NODES = "Submit a new node to be provisioned and used to execute the Jobs";
        public static final String RETRIEVES_NODES = "Retrieves all nodes submitted";

        public static final String JOB_ID = "The ID of the specific job";
        public static final String QUEUE_ID = "The ID of the specific queue";
        public static final String JDF_FILE_PATH = "The path to the jdf file";

        public static final String RETRIEVE_QUEUE = "Retrieve a specific queue";
    }

    public static class Nonce {
        public static final String TAG = "Nonce API";
        public static final String DESCRIPTION = "Manages nonces";
        public static final String GENERATE = "Generates a nonce";
    }

    public static class Version {

        public static final String DESCRIPTION =
                "Describes the current version of the Iguassu.";
        public static final String CURRENT_VERSION = "Returns the current version.";
    }

    public static class Auth {

        public static final String TAG = "Auth API";
        public static final String DESCRIPTION = "Manages authentication and authorization";
        public static final String AUTHENTICATE_USER =
                "Authenticate an user returning an Iguassu Token "
                        + "from OAuth2 Authorization Code";
        public static final String AUTHORIZATION_CODE =
                "The body of the request must specify a valid OAuth2 Authorization Code. e.g.: \"{\"authorization_code\":\"my_oauth_code\"}\"";
        public static final String REFRESH_TOKEN =
                "Refresh a OAuth Access Token";
        public static final String APPLICATION_IDENTIFIERS = "The header of request must contain the application identifiers."
                + " e.g.: \"{\"client_id\":\"FMMIxZy1Zwm0LbSxLrTvXQ1J9HChFaBDbICNrAxa2wNfe6ePSMgQWCPLbIcV74EX\", \"secret\":\"OO1vnx8xV8zmfrmgfSQDS7w3ZNmaztxPKEos7k9K6hX1aPB2H9Qpca0yYo5OiTvF\", \"redirect_uri\":\"http://127.0.0.1/auth\"}\"";
    }

    public static class PublicKey {
        public static final String TAG = "Public Key API";
        public static final String DESCRIPTION = "Provides Iguassu's public key";
        public static final String GET = "Get the public key from Iguassu";
    }

    public static class CommonParameters {

        public static final String USER_CREDENTIALS =
                "The header of the request must specify a valid Iguassu Token, "
                        + "a User Identifier and a Nonce. It is a raw json with the \"user_id\", \"iguassu_token\" and \"nonce\" keys.";
    }
}
