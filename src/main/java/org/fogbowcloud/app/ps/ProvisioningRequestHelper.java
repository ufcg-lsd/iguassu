package org.fogbowcloud.app.ps;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Properties;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.constants.ConfProperty;
import org.fogbowcloud.app.ps.Constants.Endpoint;
import org.fogbowcloud.app.ps.dto.Pool;
import org.fogbowcloud.app.utils.HttpWrapper;

public class ProvisioningRequestHelper {

    private static final Logger logger = Logger.getLogger(ProvisioningRequestHelper.class);
    private final String serviceBaseUrl;
    private final Gson jsonUtil;

    public ProvisioningRequestHelper(Properties properties) {
        serviceBaseUrl = properties.getProperty(ConfProperty.PROVISIONING_SERVICE_URL.getProp());
        this.jsonUtil = new Gson();
    }

    public void createPool(String poolName) throws Exception {
        String url = String.format(Endpoint.POOL, poolName);

        JsonObject poolBody = new JsonObject();
        poolBody.addProperty(Constants.POOL_NAME_KEY, poolName);
        StringEntity body = new StringEntity(poolBody.toString());

        String jsonResponse = HttpWrapper.doRequest(HttpPost.METHOD_NAME, url, null, body);
        JsonObject response = this.jsonUtil.fromJson(jsonResponse, JsonObject.class);
        logger.info("Response message: " + response.get(Constants.MSG_KEY));
    }

    public Pool getPool(String poolName) throws Exception {
        String url = String.format(Endpoint.POOL, poolName);
        String jsonResponse = HttpWrapper.doRequest(HttpGet.METHOD_NAME, url, null);
        Pool pool = this.jsonUtil.fromJson(jsonResponse, Pool.class);
        return pool;
    }

}
