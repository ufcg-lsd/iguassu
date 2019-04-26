package org.fogbowcloud.app.core.task;


import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.constants.FogbowConstants;
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
    private static final String USERNAME_STR = "username";
    private static final String IMAGE_STR = "image";

    private String image;
    private String username;

    private Map<String, String> requirements;

    public Specification(String image, String username) {
        this.image = image;
        this.username = username;
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

    public String getImage() {
        return this.image;
    }

    public String getUsername() {
        return this.username;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" Image: " + this.image);

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
        Specification cloneSpec = new Specification(this.image, this.username);
        cloneSpec.putAllRequirements(this.getAllRequirements());
        return cloneSpec;
    }

    public JSONObject toJSON() {
        try {
            JSONObject specification = new JSONObject();
            specification.put(IMAGE_STR, this.getImage());
            specification.put(USERNAME_STR, this.getUsername());
            specification.put(REQUIREMENTS_MAP_STR, getAllRequirements().toString());
            return specification;
        } catch (JSONException e) {
            LOGGER.debug("Error while trying to create a JSON from Specification", e);
            return null;
        }
    }

    public static Specification fromJSON(JSONObject specJSON) {
        Specification specification = new Specification(
                specJSON.optString(IMAGE_STR), specJSON.optString(USERNAME_STR)
        );
        HashMap<String, String> reqMap = (HashMap<String, String>) toMap(specJSON.optString(REQUIREMENTS_MAP_STR));
        specification.putAllRequirements(reqMap);
        return specification;
    }

    public static Map<String, String> toMap(String jsonStr) {
        return JSONUtils.toMap(jsonStr);
    }

    public String getvCPU() {
        return getFogbowRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS_Glue2vCPU);
    }

    public String getMemory() {
        return getFogbowRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS_Glue2RAM);
    }

    public String getDisk() {
        return getFogbowRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS_Glue2disk);
    }

    private String getFogbowRequirement(String fogbowRequirementKey) {
        String fogbowRequirements = getRequirementValue(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS);

        if (fogbowRequirements == null) {
            return null;
        }

        fogbowRequirements = fogbowRequirements.trim().replaceAll(" +", " ");

        boolean found = fogbowRequirements.contains(fogbowRequirementKey);

        String fogbowRequirementValue = "";
        if (found) {
            String[] strAsArray = fogbowRequirements.split(" ");
            String currentItemKey, currentItemOperator, currentItemValue;

            for (int i = 0; i < strAsArray.length; i++) {

                currentItemKey = strAsArray[i];
                currentItemOperator = strAsArray[i+1];
                currentItemValue = strAsArray[i+2];

                if (currentItemKey.equals(fogbowRequirementKey)) {
                    switch (currentItemOperator) {
                        case "<=":
                        case "==":
                        case ">=":
                            fogbowRequirementValue = currentItemValue;
                            break;
                        default:
                            break;
                    }
                    break;
                }
            }
        }
        return fogbowRequirementValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((image == null) ? 0 : image.hashCode());
        result = prime * result + ((requirements == null) ? 0 : requirements.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
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
        if (image == null) {
            if (other.image != null)
                return false;
        } else if (!image.equals(other.image))
            return false;
        if (requirements == null) {
            if (other.requirements != null)
                return false;
        } else if (!requirements.equals(other.requirements))
            return false;
        if (username == null) {
            return other.username == null;
        } else return username.equals(other.username);
    }
}
