package org.fogbowcloud.app.utils;

import java.util.Objects;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.constants.ConfProperties;
import org.fogbowcloud.app.core.exceptions.IguassuException;

public class ConfValidator {

    private static final Logger LOGGER = Logger.getLogger(ConfValidator.class);

    public static void validate(Properties properties) throws IguassuException {
        if (Objects.isNull(properties)) {
            throw new IllegalArgumentException("Properties object cannot be null.");
        }

        validateOAuthProps(properties);
        validateServicesAddresses(properties);
        validateMonitorPeriods(properties);

        LOGGER.debug("All properties are set.");
    }

    private static void validateServicesAddresses(Properties properties) throws IguassuException {
        validadeKey(properties, ConfProperties.ARREBOL_SERVICE_HOST_URL);
        validadeKey(properties, ConfProperties.DATABASE_HOST_URL);
        validadeKey(properties, ConfProperties.IGUASSU_SERVICE_HOST_URL);
        validadeKey(properties, ConfProperties.STORAGE_SERVICE_HOST_URL);
    }

    private static void validateMonitorPeriods(Properties properties) throws IguassuException {
        validadeKey(properties, ConfProperties.JOB_SUBMISSION_MONITOR_PERIOD);
        validadeKey(properties, ConfProperties.SESSION_MONITOR_PERIOD);
        validadeKey(properties, ConfProperties.JOB_STATE_MONITOR_PERIOD);
    }

    private static void validateOAuthProps(Properties properties) throws IguassuException {
        validadeKey(properties, ConfProperties.OAUTH_STORAGE_SERVICE_CLIENT_ID);
        validadeKey(properties, ConfProperties.OAUTH_STORAGE_SERVICE_CLIENT_SECRET);
        validadeKey(properties, ConfProperties.OAUTH_STORAGE_SERVICE_TOKEN_URL);
    }

    private static void validadeKey(Properties properties, String propKey) throws IguassuException {
        if (!properties.containsKey(propKey)) {
            String errorMsg = requiredPropertyMessage(propKey);
            LOGGER.error(errorMsg);
            throw new IguassuException(errorMsg);
        }
    }

    private static String requiredPropertyMessage(String property) {
        return "Required property " + property + " was not set";
    }
}
