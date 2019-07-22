package org.fogbowcloud.app.core.constants;

public class ConfProperties {

    /*
     * External OAuth props.
     */
    public static final String OAUTH_STORAGE_SERVICE_TOKEN_URL = "oauth_storage_service_token_url";
    public static final String OAUTH_STORAGE_SERVICE_CLIENT_ID = "oauth_storage_service_client_id";
    public static final String OAUTH_STORAGE_SERVICE_CLIENT_SECRET = "oauth_storage_service_client_secret";

    /*
     *  Users datastore location.
     */
    public static final String DATASTORES_USERS_FILE_PATH = "datastores/users";
    public static final String DATASTORES_USERS_DB_FILE_PATH = "datastores/users.db";

    /*
     *  Authentication and authorization custom headers.
     */
    public static final String X_AUTH_USER_CREDENTIALS = "X-Auth-User-Credentials";
    public static final String X_AUTH_APP_IDENTIFIERS = "X-Auth-App-Identifiers";

    /*
     *  Service host addresses.
     */
    public static final String ARREBOL_SERVICE_HOST_URL = "arrebol_service_host_url";
    public static final String IGUASSU_SERVICE_HOST_URL = "iguassu_service_host_url";
    public static final String STORAGE_SERVICE_HOST_URL = "storage_service_host_url";

    /*
     *  General useful paths.
     */
    public static final String IGUASSU_CONF_FILE = "iguassu.conf";
    public static final String DATABASE_HOST_URL = "database_host_url";
    public static final String JDF_FILE_PATH = "jdffilepath";

    /*
     * Monitors periods.
     */
    public static final String JOB_STATE_MONITOR_PERIOD = "job_state_monitor_period";
    public static final String JOB_SUBMISSION_MONITOR_PERIOD = "job_submission_monitor_period";
    public static final String SESSION_MONITOR_PERIOD = "session_monitor_period";
}
