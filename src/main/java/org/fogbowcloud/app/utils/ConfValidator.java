package org.fogbowcloud.app.utils;

import java.util.Objects;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.constants.ConfProperty;
import org.fogbowcloud.app.core.constants.ExitCode;

public class ConfValidator {

    private static final Logger logger = Logger.getLogger(ConfValidator.class);

    public static void validate(Properties properties) {
        if (Objects.isNull(properties)) {
            throw new IllegalArgumentException("Properties object cannot be null.");
        }

        validateOAuthProps(properties);
        validateServicesAddresses(properties);
        validateMonitorPeriods(properties);

        logger.debug("All properties are set.");
    }

    private static void validateServicesAddresses(Properties properties) {
        validatePropKey(properties, ConfProperty.ARREBOL_SERVICE_HOST_URL.getProp());
        validatePropKey(properties, ConfProperty.DATABASE_HOST_URL.getProp());
        validatePropKey(properties, ConfProperty.IGUASSU_SERVICE_HOST_URL.getProp());
        validatePropKey(properties, ConfProperty.STORAGE_SERVICE_HOST_URL.getProp());
    }

    private static void validateMonitorPeriods(Properties properties) {
        validatePropKey(properties, ConfProperty.JOB_SUBMISSION_MONITOR_PERIOD.getProp());
        validatePropKey(properties, ConfProperty.SESSION_MONITOR_PERIOD.getProp());
        validatePropKey(properties, ConfProperty.JOB_STATE_MONITOR_PERIOD.getProp());
    }

    private static void validateOAuthProps(Properties properties) {
        validatePropKey(properties, ConfProperty.OAUTH_STORAGE_SERVICE_CLIENT_ID.getProp());
        validatePropKey(properties, ConfProperty.OAUTH_STORAGE_SERVICE_CLIENT_SECRET.getProp());
        validatePropKey(properties, ConfProperty.OAUTH_STORAGE_SERVICE_TOKEN_URL.getProp());
    }

    private static void validatePropKey(Properties properties, String propKey) {
        if (!properties.containsKey(propKey)) {
            String errorMsg = requiredPropertyMessage(propKey);
            logger.error(errorMsg);

            System.exit(ExitCode.FAIL.getCode());
        }
    }

    private static String requiredPropertyMessage(String propertyKey) {
        return "Required property " + propertyKey + " not found.";
    }
}
