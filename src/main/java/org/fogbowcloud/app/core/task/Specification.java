package org.fogbowcloud.app.core.task;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.constants.JsonKey;
import org.fogbowcloud.app.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/** Sets meta information about Jobs. */
public class Specification implements Serializable {

    private static final long serialVersionUID = 5255295548723927267L;
    private static final Logger logger = Logger.getLogger(Specification.class);

    private static final String LN = System.lineSeparator();

    private String dockerImage;
    private String userId;

    private Map<String, String> requirements;

    public Specification(String dockerImage, String userId) {
        this.dockerImage = dockerImage;
        this.userId = userId;
        this.requirements = new HashMap<>();
    }

    public static Specification fromJSON(JSONObject specJSON) {
        Specification specification =
                new Specification(
                        specJSON.optString(JsonKey.DOCKER_IMAGE.getKey()),
                        specJSON.optString(JsonKey.USER_ID.getKey()));
        HashMap<String, String> reqMap =
                (HashMap<String, String>) toMap(specJSON.optString(JsonKey.REQUIREMENTS.getKey()));
        specification.putAllRequirements(reqMap);
        return specification;
    }

    private static Map<String, String> toMap(String jsonStr) {
        return JSONUtils.toMap(jsonStr);
    }

    public void addRequirement(String key, String value) {
        this.requirements.put(key, value);
    }

    public String getRequirementValue(String key) {
        return this.requirements.get(key);
    }

    private void putAllRequirements(Map<String, String> requirements) {
        for (Map.Entry<String, String> e : requirements.entrySet()) {
            this.requirements.put(e.getKey(), e.getValue());
        }
    }

    private Map<String, String> getAllRequirements() {
        return this.requirements;
    }

    private String getDockerImage() {
        return this.dockerImage;
    }

    public String getUserId() {
        return this.userId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" Image: ").append(this.dockerImage);

        if ((this.requirements != null) && !this.requirements.isEmpty()) {
            sb.append(LN).append("Requirements:{");
            for (Map.Entry<String, String> entry : this.requirements.entrySet()) {
                sb.append(LN)
                        .append("\t")
                        .append(entry.getKey())
                        .append(": ")
                        .append(entry.getValue());
            }
            sb.append(LN).append("}");
        }
        return sb.toString();
    }

    public JSONObject toJSON() {
        try {
            JSONObject specification = new JSONObject();
            specification.put(JsonKey.DOCKER_IMAGE.getKey(), this.getDockerImage());
            specification.put(JsonKey.USER_ID.getKey(), this.getUserId());
            specification.put(JsonKey.REQUIREMENTS.getKey(), getAllRequirements().toString());
            return specification;
        } catch (JSONException e) {
            logger.debug("Error while trying to create a JSON from Specification", e);
            return null;
        }
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
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Specification other = (Specification) obj;
        if (dockerImage == null) {
            if (other.dockerImage != null) return false;
        } else if (!dockerImage.equals(other.dockerImage)) return false;
        if (requirements == null) {
            if (other.requirements != null) return false;
        } else if (!requirements.equals(other.requirements)) return false;
        if (userId == null) {
            return other.userId == null;
        } else return userId.equals(other.userId);
    }
}
