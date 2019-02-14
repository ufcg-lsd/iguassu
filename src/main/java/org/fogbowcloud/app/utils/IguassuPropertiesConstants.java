package org.fogbowcloud.app.utils;

public class IguassuPropertiesConstants {

	public static final String DEFAULT_SPECS_FILE_PATH = "default_specs_file_path";
	public static final String REMOVE_PREVIOUS_RESOURCES = "remove_previous_resources";

	// __________ INFRASTRUCTURE CONSTANTS __________ //
	public static final String INFRA_IS_STATIC = "infra_is_elastic";
	public static final String INFRA_PROVIDER_CLASS_NAME = "infra_provider_class_name";
	public static final String INFRA_MANAGEMENT_SERVICE_TIME = "infra_management_service_time";
	public static final String INFRA_PROVIDER_USERNAME = "provider_username";
	public static final String INFRA_RESOURCE_SERVICE_TIME = "infra_resource_service_time";
	public static final String INFRA_RESOURCE_CONNECTION_TIMEOUT = "infra_resource_connection_timeout";
	public static final String INFRA_RESOURCE_IDLE_LIFETIME = "infra_resource_idle_lifetime";
	public static final String INFRA_RESOURCE_REUSE_TIMES = "max_resource_reuse";

	public static final String INFRA_INITIAL_SPECS_FILE_PATH = "infra_initial_specs_file_path";
	public static final String INFRA_INITIAL_SPECS_BLOCK_CREATING = "infra_initial_specs_block_creating";
	public static final String INFRA_INITIAL_SPECS_REMOVE_PREVIOUS_RESOURCES = "infra_initial_specs_remove_previous_resources";	

	// __________ FOGBOW INFRASTRUCTURE CONSTANTS __________ //
	public static final String INFRA_FOGBOW_USERNAME = "fogbow_username";
	public static final String INFRA_FOGBOW_MANAGER_BASE_URL = "infra_fogbow_manager_base_url";
	public static final String INFRA_FOGBOW_TOKEN_PUBLIC_KEY_FILEPATH = "infra_fogbow_token_public_key_filepath";
	public static final String INFRA_FOGBOW_TOKEN_UPDATE_PLUGIN = "infra_fogbow_token_update_plugin";
	
	public static final String EXECUTION_MONITOR_PERIOD = "execution_monitor_period";
	public static final String REST_SERVER_PORT = "rest_server_port";

	//___________ DB CONSTANTS ______________//
	public static final String DB_MAP_NAME = "jobMap";
	public static final String DB_FILE_NAME = "legacyJobs.db";
	public static final String DB_MAP_USERS = "datastores/users";
	public static final String DB_FILE_USERS = "datastores/users.db";
	
	public static final String PUBLIC_KEY_CONSTANT = "public_key";	
	public static final String PRIVATE_KEY_FILEPATH = "private_key_filepath";
	public static final String LOCAL_OUTPUT_FOLDER = "local_output";
	public static final String REMOTE_OUTPUT_FOLDER = "remote_output_folder";
	public static final String ENCRYPTION_TYPE = "encryption_type";

	//___________ APPLICATION HEADERS  ____//	
	public static final String X_AUTH_NONCE = "X-auth-nonce";
	public static final String X_AUTH_USER = "X-auth-username";
	public static final String X_AUTH_HASH = "X-auth-hash";
	public static final String X_CREDENTIALS = "X-auth-credentials";
	public static final String AUTHENTICATION_PLUGIN = "authentication_plugin";
	public static final String LDAP_AUTHENTICATION_URL = "iguassu.ldap.auth.url";
	public static final String LDAP_AUTHENTICATION_BASE = "iguassu.ldap.base";
	
	//___________ IGUASSU CONSTANTS ____//
	public static final String JOB_ID  = "jobID";
	public static final String OWNER = "owner";

	public static final String M_IP = "m_public_ip"; // machine's public ip where this app will be deployed
	public static final String FILE_DRIVER_HOST_IP = "file_driver_host_ip";
}
