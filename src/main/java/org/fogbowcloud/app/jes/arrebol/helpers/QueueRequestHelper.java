package org.fogbowcloud.app.jes.arrebol.helpers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.Properties;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.constants.ConfProperty;
import org.fogbowcloud.app.jes.arrebol.constants.Constants.Endpoint;
import org.fogbowcloud.app.jes.arrebol.dtos.QueueDTO;
import org.fogbowcloud.app.jes.arrebol.models.QueueSpec;
import org.fogbowcloud.app.jes.exceptions.ArrebolConnectException;
import org.fogbowcloud.app.jes.exceptions.QueueCreationException;
import org.fogbowcloud.app.utils.HttpWrapper;

public class QueueRequestHelper {

    private static final Logger LOGGER = Logger.getLogger(QueueRequestHelper.class);
    private final String serviceBaseUrl;
    private final Gson gson;

    public QueueRequestHelper(Properties properties) {
        serviceBaseUrl = properties.getProperty(ConfProperty.ARREBOL_SERVICE_HOST_URL.getProp());
        this.gson = new Gson();
    }

    public String createQueue(QueueSpec queueSpec) throws UnsupportedEncodingException {
        final String QUEUE_ID_JSON_KEY = "id";
        final String endpoint = String.format(Endpoint.QUEUES, serviceBaseUrl);
        StringEntity requestBody;
        String queueId;

        try {
            requestBody = makeJSONBody(queueSpec);
            final String jsonResponse = HttpWrapper
                .doRequest(HttpPost.METHOD_NAME, endpoint, new LinkedList<>(), requestBody);
            JsonObject jobResponse = this.gson.fromJson(jsonResponse, JsonObject.class);
            queueId = jobResponse.get(QUEUE_ID_JSON_KEY).getAsString();
            LOGGER.info("Queue [" + queueId + "] was created with success to Arrebol.");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedEncodingException("Queue is not well formed to built JSON.");
        } catch (HttpHostConnectException e) {
            throw new ArrebolConnectException("Failed connect to Arrebol: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new QueueCreationException(
                "Creation queue to Arrebol has failed: " + e.getMessage(), e);
        }

        return queueId;
    }

    public QueueDTO getQueue(String queueId) {
        final String endpoint = String.format(Endpoint.QUEUE, serviceBaseUrl, queueId);
        QueueDTO queue;
        try {
            final String jsonResponse = HttpWrapper
                .doRequest(HttpGet.METHOD_NAME, endpoint, new LinkedList<>(), null);
            queue = this.gson.fromJson(jsonResponse, QueueDTO.class);
            return queue;
        } catch (HttpHostConnectException e) {
            throw new ArrebolConnectException("Failed connect to Arrebol: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Getting Queue from Arrebol has FAILED: " + e.getMessage());
        }
    }

    private StringEntity makeJSONBody(QueueSpec queueSpec) throws UnsupportedEncodingException {
        LOGGER.info("Building JSON body of Queue!");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(queueSpec);

        return new StringEntity(json);
    }
}
