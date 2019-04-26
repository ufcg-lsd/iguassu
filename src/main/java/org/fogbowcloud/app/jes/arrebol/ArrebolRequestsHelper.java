package org.fogbowcloud.app.jes.arrebol;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.constants.IguassuPropertiesConstants;
import org.fogbowcloud.app.core.dto.JobDTO;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jes.http.HttpWrapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

// TODO implement tests
public class ArrebolRequestsHelper {

	// TODO review this names
    private final Properties properties;
    private final String arrebolBaseUrl;
    private final Gson gson = new Gson();
    
    private static final Logger LOGGER = Logger.getLogger(ArrebolRequestsHelper.class);

    public ArrebolRequestsHelper(Properties properties) {
        this.properties = properties;
        this.arrebolBaseUrl = this.properties.getProperty(IguassuPropertiesConstants.ARREBOL_BASE_URL);
    }

    public String submitJobToExecution(JDFJob job) {
        StringEntity requestBody = null;

        try {
            requestBody = makeJSONBody(job);
        } catch (UnsupportedEncodingException e) {
        	// TODO throw exception
        }
        
        String jsonResponse = null;
        try {
			jsonResponse = HttpWrapper.doRequest(HttpPost.METHOD_NAME, this.arrebolBaseUrl, null, requestBody);
		} catch (Exception e) {
        	// TODO throw exception
		}
        JsonObject jobResponse = this.gson.fromJson(jsonResponse, JsonObject.class);
                
        // Use constants
        return jobResponse.get("id").getAsString();
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
