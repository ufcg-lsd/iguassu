package org.fogbowcloud.app.jes.arrebol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Properties;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.constants.ConfProperty;
import org.fogbowcloud.app.core.dto.arrebol.ArrebolExecutionDTO;
import org.fogbowcloud.app.core.http.HttpWrapper;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jes.exceptions.ArrebolConnectException;
import org.fogbowcloud.app.jes.exceptions.GetJobException;
import org.fogbowcloud.app.jes.exceptions.SubmitJobException;

// TODO implement tests
final class ArrebolRequestsHelper {

    private static final Logger logger = Logger.getLogger(ArrebolRequestsHelper.class);
    private final String serviceBaseUrl;
    private final Gson jsonUtil;

    ArrebolRequestsHelper(Properties properties) {
        serviceBaseUrl = properties.getProperty(ConfProperty.ARREBOL_SERVICE_HOST_URL.getProp());
        this.jsonUtil = new Gson();
    }

    String submitToExecution(JDFJob job)
            throws UnsupportedEncodingException, SubmitJobException, ArrebolConnectException {
        StringEntity requestBody;

        try {
            requestBody = makeJSONBody(job);
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedEncodingException(
                    "Job with id [" + job.getId() + "] is not well formed to built JSON.");
        }

        final String jobEndpoint = serviceBaseUrl + "/job";

        String jobIdArrebol;
        final String JSON_KEY_JOB_ID_ARREBOL = "id";

        try {
            final String jsonResponse =
                    HttpWrapper.doRequest(
                            HttpPost.METHOD_NAME, jobEndpoint, new LinkedList<>(), requestBody);

            JsonObject jobResponse = this.jsonUtil.fromJson(jsonResponse, JsonObject.class);

            jobIdArrebol = jobResponse.get(JSON_KEY_JOB_ID_ARREBOL).getAsString();

            logger.info("Job [" + job.getId() + "] was submitted with success to Arrebol.");
        } catch (HttpHostConnectException e) {
            throw new ArrebolConnectException("Failed connect to Arrebol: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new SubmitJobException(
                    "Submit Job to Arrebol has status FAILED: " + e.getMessage(), e);
        }

        return Objects.requireNonNull(jobIdArrebol);
    }

    ArrebolExecutionDTO getJob(String jobArrebolId) throws GetJobException {
        return this.jsonUtil.fromJson(getJobJSON(jobArrebolId), ArrebolExecutionDTO.class);
    }

    String getJobJSON(String jobArrebolId) throws GetJobException {
        final String endpoint = serviceBaseUrl + "/job/" + jobArrebolId;

        String jsonResponse;
        try {
            jsonResponse = HttpWrapper.doRequest(HttpGet.METHOD_NAME, endpoint, null);
        } catch (Exception e) {
            throw new GetJobException("Getting Job from Arrebol has FAILED: " + e.getMessage());
        }

        return jsonResponse;
    }

    private StringEntity makeJSONBody(JDFJob job) throws UnsupportedEncodingException {
        logger.info("Building JSON body of Job : [" + job.getId() + "]");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ArrebolExecutionDTO arrebolExecutionDTO = new ArrebolExecutionDTO(job);
        String json = gson.toJson(arrebolExecutionDTO);

        logger.debug("Job json looks like : \n" + json);

        return new StringEntity(json);
    }
}
