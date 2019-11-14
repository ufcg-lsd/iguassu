package org.fogbowcloud.app.ps;

import com.google.gson.Gson;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.constants.ConfProperty;

import java.util.Properties;

public class ProvisioningRequestHelper {

    private static final Logger logger = Logger.getLogger(ProvisioningRequestHelper.class);
    private final String serviceBaseUrl;
    private final Gson jsonUtil;

    public ProvisioningRequestHelper(Properties properties) {
        serviceBaseUrl = properties.getProperty(ConfProperty.PROVISIONING_SERVICE_URL.getProp());
        this.jsonUtil = new Gson();
    }


}
