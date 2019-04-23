package org.fogbowcloud.app.core.constants;

public class IguassuPropertiesConstants {

	public static final String DEFAULT_COMPUTE_FLAVOR_SPEC = "default_compute_flavor_spec";
	public static final String DEFAULT_CLOUD_NAME = "default-cloud-name";
	public static final String REMOVE_PREVIOUS_RESOURCES = "remove_previous_resources";
	public static final String INFRA_PROVIDER_USERNAME = "provider_username";
	public static final String EXECUTION_MONITOR_PERIOD = "execution_monitor_period";
	public static final String REST_SERVER_PORT = "rest_server_port";
	public static final String DATASTORES_USERS = "datastores/users";
	public static final String DATASTORES_USERS_DB = "datastores/users.db";
	
	public static final String IGUASSU_PUBLIC_KEY = "iguassu_public_key";
	public static final String IGUASSU_PRIVATE_KEY_FILEPATH = "iguassu_private_key_filepath";
	public static final String LOCAL_OUTPUT_FOLDER = "local_output";
	public static final String REMOTE_OUTPUT_FOLDER = "remote_output_folder";
	public static final String ENCRYPTION_TYPE = "encryption_type";

	public static final String X_CREDENTIALS = "X-auth-credentials";
	public static final String AUTHENTICATION_PLUGIN = "authentication_plugin";
	public static final String LDAP_AUTHENTICATION_URL = "iguassu.ldap.auth.url";
	public static final String LDAP_AUTHENTICATION_BASE = "iguassu.ldap.base";

	public static final String JOB_ID  = "jobID";
	public static final String OWNER = "owner";

	//____ Storage Service ____//
	public static final String IGUASSU_SERVICE_HOST = "iguassu_service_host";
	public static final String STORAGE_SERVICE_HOST = "storage_service_host";

	public static final String POOL_PLUGIN = "blowout_pool_plugin";
	public static final String SCHEDULER_PLUGIN = "blowout_scheduler_plugin";
	public static final String INFRA_MANAGER_PLUGIN = "blowout_infra_manager_plugin";
	public static final String INFRA_PROVIDER_PLUGIN = "blowout_infra_provider_plugin";
	public static final String TOKEN_UPDATE_PLUGIN = "blowout_token_update_plugin";

	public static final String INFRA_IS_ELASTIC = "infra_is_elastic";
	public static final String INFRA_RESOURCE_CONNECTION_TIMEOUT = "infra_resource_connection_timeout";
	public static final String INFRA_RESOURCE_IDLE_LIFETIME = "infra_resource_idle_lifetime";
	public static final String INFRA_RESOURCE_REUSE_TIMES = "max_resource_reuse";
	public static final String INFRA_RESOURCE_CONNECTION_RETRY = "max_resource_connection_retry";
	public static final String RESOURCE_MONITOR_SLEEP_PERIOD = "resource_monitor_sleep_period";

	public static final String AS_TOKEN_PREFIX = "as_token_";
	public static final String AS_TOKEN_USERNAME = "username";
	public static final String AS_TOKEN_PASSWORD = "password";
	public static final String AS_TOKEN_PROJECT_NAME = "projectname";
	public static final String AS_TOKEN_DOMAIN = "domain";
	public static final String AS_TOKEN_PUBLIC_KEY = "publicKey";
	public static final String AS_TOKEN_CREDENTIALS = "credentials";
	public static final String AS_BASE_URL = "as_base_url";

	public static final String RAS_BASE_URL = "ras_base_url";
	public static final String RAS_MEMBER_ID = "ras_member_id";

	public static final String TOKEN_UPDATE_TIME = "ras_token_update_time";
	public static final String TOKEN_UPDATE_TIME_UNIT = "ras_token_update_time_unit";
}
