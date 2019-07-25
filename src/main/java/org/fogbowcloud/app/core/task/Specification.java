package org.fogbowcloud.app.core.task;


import org.apache.log4j.Logger;
import org.fogbowcloud.app.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Specification implements Serializable {

    private static final long serialVersionUID = 5255295548723927267L;
    private static final Logger LOGGER = Logger.getLogger(Specification.class);

    private static final String LN = System.lineSeparator();
    private static final String REQUIREMENTS_MAP_STR = "requirementsMap";
    private static final String USER_ID_STR = "userId";
    private static final String IMAGE_STR = "image";

    private String dockerImage;
    private String userId;

    private Map<String, String> requirements;

    public Specification(String dockerImage, String userId) {
        this.dockerImage = dockerImage;
        this.userId = userId;
        this.requirements = new HashMap<>();
    }

    public void addRequirement(String key, String value) {
        this.requirements.put(key, value);
    }

    public String getRequirementValue(String key) {
        return this.requirements.get(key);
    }

    public void putAllRequirements(Map<String, String> requirements) {
        for (Map.Entry<String, String> e : requirements.entrySet()) {
            this.requirements.put(e.getKey(), e.getValue());
        }
    }

    public Map<String, String> getAllRequirements() {
        return this.requirements;
    }

    public String getDockerImage() {
        return this.dockerImage;
    }

    public String getUserId() {
        return this.userId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" Image: " + this.dockerImage);

        if ((this.requirements != null) && !this.requirements.isEmpty()) {
            sb.append(LN + "Requirements:{");
            for (Map.Entry<String, String> entry : this.requirements.entrySet()) {
                sb.append(LN + "\t" + entry.getKey() + ": " + entry.getValue());
            }
            sb.append(LN + "}");
        }
        return sb.toString();
    }

    public Specification clone() {
        Specification cloneSpec = new Specification(this.dockerImage, this.userId);
        cloneSpec.putAllRequirements(this.getAllRequirements());
        return cloneSpec;
    }

    public JSONObject toJSON() {
        try {
            JSONObject specification = new JSONObject();
            specification.put(IMAGE_STR, this.getDockerImage());
            specification.put(USER_ID_STR, this.getUserId());
            specification.put(REQUIREMENTS_MAP_STR, getAllRequirements().toString());
            return specification;
        } catch (JSONException e) {
            LOGGER.debug("Error while trying to create a JSON from Specification", e);
            return null;
        }
    }

    public static Specification fromJSON(JSONObject specJSON) {
        Specification specification = new Specification(
                specJSON.optString(IMAGE_STR), specJSON.optString(USER_ID_STR)
        );
        HashMap<String, String> reqMap = (HashMap<String, String>) toMap(specJSON.optString(REQUIREMENTS_MAP_STR));
        specification.putAllRequirements(reqMap);
        return specification;
    }

    public static Map<String, String> toMap(String jsonStr) {
        return JSONUtils.toMap(jsonStr);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dockerImage == null) ? 0 : dockerImage.hashCode());
        result = prime * result + ((requirements == null) ? 0 : requirements.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Specification other = (Specification) obj;
        if (dockerImage == null) {
            if (other.dockerImage != null)
                return false;
        } else if (!dockerImage.equals(other.dockerImage))
            return false;
        if (requirements == null) {
            if (other.requirements != null)
                return false;
        } else if (!requirements.equals(other.requirements))
            return false;
        if (userId == null) {
            return other.userId == null;
        } else return userId.equals(other.userId);
    }
}
