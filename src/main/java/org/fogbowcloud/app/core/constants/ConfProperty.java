package org.fogbowcloud.app.core.constants;

public enum ConfProperty {
    OAUTH_STORAGE_SERVICE_TOKEN_URL("oauth_storage_service_token_url"),
    OAUTH_STORAGE_SERVICE_CLIENT_ID("oauth_storage_service_client_id"),
    OAUTH_STORAGE_SERVICE_CLIENT_SECRET("oauth_storage_service_client_secret"),
    ARREBOL_SERVICE_HOST_URL("arrebol_service_host_url"),
    IGUASSU_SERVICE_HOST_URL("iguassu_service_host_url"),
    STORAGE_SERVICE_HOST_URL("storage_service_host_url"),
    JOB_STATE_MONITOR_PERIOD("job_state_monitor_period"),
    JOB_SUBMISSION_MONITOR_PERIOD("job_submission_monitor_period"),
    SESSION_MONITOR_PERIOD("session_monitor_period");

    private final String property;

    ConfProperty(String property) {
        this.property = property;
    }

    public String getProp() {
        return this.property;
    }
}
