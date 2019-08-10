package org.fogbowcloud.app.jes.arrebol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.constants.ConfProperty;
import org.fogbowcloud.app.jes.arrebol.dtos.ArrebolExecutionDTO;
import org.fogbowcloud.app.jes.arrebol.models.JobExecArrebol;
import org.fogbowcloud.app.core.models.job.Job;
import org.fogbowcloud.app.jes.exceptions.ArrebolConnectException;
import org.fogbowcloud.app.jes.exceptions.JobExecStatusException;
import org.fogbowcloud.app.jes.exceptions.JobSubmissionException;
import org.fogbowcloud.app.utils.HttpWrapper;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Properties;

/**
 * A utility class that encapsulates communication logic, that is, requests, with the Job Execution
 * Service.
 */
final class ArrebolRequestsHelper {

    private static final Logger logger = Logger.getLogger(ArrebolRequestsHelper.class);
    private final String serviceBaseUrl;
    private final Gson jsonUtil;

    ArrebolRequestsHelper(Properties properties) {
        serviceBaseUrl = properties.getProperty(ConfProperty.ARREBOL_SERVICE_HOST_URL.getProp());
        this.jsonUtil = new Gson();
    }

    /**
     * Submit an job to execution in the Execution Service.
     *
     * @param job to be performed.
     * @return an execution identifier.
     * @throws UnsupportedEncodingException if the job in the params has a bad shape.
     * @throws JobSubmissionException       if the post request failed.
     * @throws ArrebolConnectException      if the Arrebol Job Execution Service is down.
     */
    String submitToExecution(Job job)
            throws UnsupportedEncodingException, JobSubmissionException, ArrebolConnectException {
        StringEntity requestBody;

        try {
            requestBody = makeJSONBody(job);
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedEncodingException(
                    "Job with id [" + job.getId() + "] is not well formed to built JSON.");
        }

        final String jobEndpoint = serviceBaseUrl + "/job";

        String executionId;
        final String JSON_KEY_JOB_ID_ARREBOL = "id";

        try {
            final String jsonResponse =
                    HttpWrapper.doRequest(
                            HttpPost.METHOD_NAME, jobEndpoint, new LinkedList<>(), requestBody);

            JsonObject jobResponse = this.jsonUtil.fromJson(jsonResponse, JsonObject.class);

            executionId = jobResponse.get(JSON_KEY_JOB_ID_ARREBOL).getAsString();

            logger.info("Job [" + job.getId() + "] was submitted with success to Arrebol.");
        } catch (HttpHostConnectException e) {
            throw new ArrebolConnectException("Failed connect to Arrebol: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new JobSubmissionException(
                    "Submit Job to Arrebol has status FAILED: " + e.getMessage(), e);
        }

        return Objects.requireNonNull(executionId);
    }

    /**
     * Takes the status of the execution of a job.
     *
     * @param executionId is the identifier of the execution. Each job has a unique execution
     *                    identifier.
     * @return the current execution state in json string.
     * @throws JobExecStatusException  if the get request failed.
     * @throws ArrebolConnectException if the Arrebol Job Execution Service is down.
     */
    JobExecArrebol getExecutionStatus(String executionId)
            throws JobExecStatusException, ArrebolConnectException {
        final String endpoint = serviceBaseUrl + "/job/" + executionId;

        String jsonResponse;
        JobExecArrebol jobExecArrebol;

        try {
            jsonResponse = HttpWrapper.doRequest(HttpGet.METHOD_NAME, endpoint, null);

            jobExecArrebol = this.jsonUtil.fromJson(jsonResponse, JobExecArrebol.class);
        } catch (HttpHostConnectException e) {
            throw new ArrebolConnectException("Failed connect to Arrebol: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new JobExecStatusException("Getting Job from Arrebol has FAILED: " + e.getMessage());
        }

        return jobExecArrebol;
    }

    private StringEntity makeJSONBody(Job job) throws UnsupportedEncodingException {
        logger.info("Building JSON body of Job : [" + job.getId() + "]");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ArrebolExecutionDTO arrebolExecutionDTO = new ArrebolExecutionDTO(job);
        String json = gson.toJson(arrebolExecutionDTO);

        return new StringEntity(json);
    }
}
