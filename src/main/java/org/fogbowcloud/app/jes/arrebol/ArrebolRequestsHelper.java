package org.fogbowcloud.app.jes.arrebol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.command.Command;
import org.fogbowcloud.app.core.constants.IguassuPropertiesConstants;
import org.fogbowcloud.app.core.dto.JobDTO;
import org.fogbowcloud.app.core.task.Specification;
import org.fogbowcloud.app.core.task.Task;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jes.exceptions.SubmitJobException;
import org.fogbowcloud.app.jes.http.HttpWrapper;
import org.fogbowcloud.app.utils.JSONUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;


public class ArrebolRequestsHelper {

    private static final Logger LOGGER = Logger.getLogger(ArrebolRequestsHelper.class);
    private final Properties properties;
    private final String ARREBOL_BASE_URL;

    public ArrebolRequestsHelper(Properties properties) {
        this.properties = properties;
        ARREBOL_BASE_URL = this.properties.getProperty(IguassuPropertiesConstants.ARREBOL_BASE_URL);
    }

    public String submitJobToExecution(JDFJob job) throws Exception, SubmitJobException {
        StringEntity requestBody = null;

        try {
            requestBody = makeJSONBody(job);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        final String jobEndpoint = ARREBOL_BASE_URL + "/job";

        String jobIdArrebol;

        try {
            final String jsonResponse = HttpWrapper.doRequest(HttpPost.METHOD_NAME, jobEndpoint,
                    new LinkedList<>(), requestBody);
            jobIdArrebol = JSONUtils.getValueFromJsonStr("id", jsonResponse);
            LOGGER.info("Job was submitted with success.");
        } catch (Exception e) {
            throw new SubmitJobException("Submit Job to Arrebol has FAILED: " + e.getMessage(), e);
        }

        return jobIdArrebol;
    }

    public JDFJob getJob(String jobId) {
        return null;
    }

    public StringEntity makeJSONBody(JDFJob job) throws UnsupportedEncodingException {
        LOGGER.info("Building JSON body of Job : [" + job.getId() + "]");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JobDTO jobDTO = new JobDTO((job));
        String json = gson.toJson(jobDTO);

        LOGGER.info("Job json looks like " + json);

        return new StringEntity(json);
    }
}
