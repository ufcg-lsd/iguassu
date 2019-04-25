package org.fogbowcloud.app.jes.arrebol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.command.Command;
import org.fogbowcloud.app.core.constants.IguassuPropertiesConstants;
import org.fogbowcloud.app.core.dto.JobDTO;
import org.fogbowcloud.app.core.task.Specification;
import org.fogbowcloud.app.core.task.Task;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jes.http.HttpWrapper;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Properties;



public class ArrebolRequestsHelper {

    private static final Logger LOGGER = Logger.getLogger(ArrebolRequestsHelper.class);
    private final Properties properties;
    private final String ARREBOL_BASE_URL;
    private final HttpWrapper http;

    public ArrebolRequestsHelper(Properties properties) {
        this.properties = properties;
        this.http = new HttpWrapper();
        ARREBOL_BASE_URL = this.properties.getProperty(IguassuPropertiesConstants.ARREBOL_BASE_URL);
    }

    public String submitJobToExecution(JDFJob job) {
        StringEntity requestBody = null;

        try {
            requestBody = makeJSONBody(job);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
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
