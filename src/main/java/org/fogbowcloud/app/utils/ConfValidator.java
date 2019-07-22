package org.fogbowcloud.app.utils;

import java.util.Objects;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.constants.ConfProperties;

public class ConfValidator {

    private static final Logger LOGGER = Logger.getLogger(ConfValidator.class);

    public static void validate(Properties properties) {
        if (Objects.isNull(properties)) {
            throw new IllegalArgumentException("Properties object cannot be null.");
        }

        validateOAuthProps(properties);
        validateServicesAddresses(properties);
        validateMonitorPeriods(properties);

        LOGGER.debug("All properties are set.");
    }

    private static void validateServicesAddresses(Properties properties) {
        validatePropKey(properties, ConfProperties.ARREBOL_SERVICE_HOST_URL);
        validatePropKey(properties, ConfProperties.DATABASE_HOST_URL);
        validatePropKey(properties, ConfProperties.IGUASSU_SERVICE_HOST_URL);
        validatePropKey(properties, ConfProperties.STORAGE_SERVICE_HOST_URL);
    }

    private static void validateMonitorPeriods(Properties properties) {
        validatePropKey(properties, ConfProperties.JOB_SUBMISSION_MONITOR_PERIOD);
        validatePropKey(properties, ConfProperties.SESSION_MONITOR_PERIOD);
        validatePropKey(properties, ConfProperties.JOB_STATE_MONITOR_PERIOD);
    }

    private static void validateOAuthProps(Properties properties) {
        validatePropKey(properties, ConfProperties.OAUTH_STORAGE_SERVICE_CLIENT_ID);
        validatePropKey(properties, ConfProperties.OAUTH_STORAGE_SERVICE_CLIENT_SECRET);
        validatePropKey(properties, ConfProperties.OAUTH_STORAGE_SERVICE_TOKEN_URL);
    }

    private static void validatePropKey(Properties properties, String propKey) {
        if (!properties.containsKey(propKey)) {
            String errorMsg = requiredPropertyMessage(propKey);
            LOGGER.error(errorMsg);
            System.exit(1);
        }
    }

    private static String requiredPropertyMessage(String propertyKey) {
        return "Required property " + propertyKey + " not found.";
    }
}
