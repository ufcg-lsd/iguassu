package org.fogbowcloud.app.jes.arrebol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.Properties;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.constants.IguassuPropertiesConstants;
import org.fogbowcloud.app.core.dto.arrebol.ArrebolJobDTO;
import org.fogbowcloud.app.core.http.HttpWrapper;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jes.exceptions.GetJobException;
import org.fogbowcloud.app.jes.exceptions.SubmitJobException;

// TODO implement tests
public class ArrebolRequestsHelper {

    private static final Logger LOGGER = Logger.getLogger(ArrebolRequestsHelper.class);
    private final String ARREBOL_BASE_URL;
    private final Gson gson;

    public ArrebolRequestsHelper(Properties properties) {
        // TODO review this names
        ARREBOL_BASE_URL = properties.getProperty(IguassuPropertiesConstants.ARREBOL_BASE_URL);
        this.gson = new Gson();
    }

    public String submitJobToExecution(JDFJob job) throws Exception, SubmitJobException {
        StringEntity requestBody;

        try {
            requestBody = makeJSONBody(job);
        } catch (UnsupportedEncodingException e) {
            throw new Exception(
                "Job with id [" + job.getId() + "] is not well formed to built JSON.");
        }

        final String jobEndpoint = ARREBOL_BASE_URL + "/job";

        String jobIdArrebol;
        final String JSON_KEY_JOB_ID_ARREBOL = "id";

        try {
            final String jsonResponse = HttpWrapper.doRequest(HttpPost.METHOD_NAME, jobEndpoint,
                new LinkedList<>(), requestBody);

            JsonObject jobResponse = this.gson.fromJson(jsonResponse, JsonObject.class);

            jobIdArrebol = jobResponse.get(JSON_KEY_JOB_ID_ARREBOL).getAsString();

            LOGGER.info("Job [" + job.getId() + "] was submitted with success to Arrebol.");

        } catch (Exception e) {
            throw new SubmitJobException("Submit Job to Arrebol has FAILED: " + e.getMessage(), e);
        }

        return jobIdArrebol;
    }

    public ArrebolJobDTO getJob(String jobArrebolId) throws GetJobException {
        return this.gson.fromJson(getJobJSON(jobArrebolId), ArrebolJobDTO.class);
    }

    public String getJobJSON(String jobArrebolId) throws GetJobException {
        final String endpoint = ARREBOL_BASE_URL + "/job/" + jobArrebolId;

        String jsonResponse;
        try {
            jsonResponse = HttpWrapper.doRequest(HttpGet.METHOD_NAME, endpoint, null);
        } catch (Exception e) {
            throw new GetJobException("Getting Job from Arrebol has FAILED: " + e.getMessage(), e);
        }

        return jsonResponse;
    }

    private StringEntity makeJSONBody(JDFJob job) throws UnsupportedEncodingException {
        LOGGER.info("Building JSON body of Job : [" + job.getId() + "]");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ArrebolJobDTO arrebolJobDTO = new ArrebolJobDTO(job);
        String json = gson.toJson(arrebolJobDTO);

        LOGGER.debug("Job json looks like : \n" + json);

        return new StringEntity(json);
    }
}
