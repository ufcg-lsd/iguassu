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

    // Todo remove these constants to a separated file.
    private static final String LN = System.lineSeparator();
    private static final String REQUIREMENTS_MAP_STR = "requirementsMap";
    private static final String USER_DATA_TYPE_STR = "userDataType";
    private static final String USER_DATA_FILE_STR = "userDataFile";
    private static final String CONTEXT_SCRIPT_STR = "contextScript";
    private static final String PRIVATE_KEY_FILE_PATH_STR = "privateKeyFilePath";
    private static final String PUBLIC_KEY_STR = "publicKey";
    private static final String USERNAME_STR = "username";
    private static final String IMAGE_STR = "image";
    private static final String CLOUD_NAME_STR = "cloudName";
    private static final Logger LOGGER = Logger.getLogger(Specification.class);

    private String cloudName;
    private String imageName;
    private String username;
    private String privateKeyFilePath;
    private String publicKey;

    //TODO Analyze the removal of these attributes
    private String contextScript;
    private String userDataFile;
    private String userDataType;

    private Map<String, String> requirements;

    public Specification(String imageName, String username, String publicKey, String privateKeyFilePath) {
        this.cloudName = null;
        this.imageName = imageName;
        this.username = username;
        this.publicKey = publicKey;
        this.privateKeyFilePath = privateKeyFilePath;
        this.requirements = new HashMap<>();
    }

    public Specification(String cloudName, String imageName, String username, String publicKey, String privateKeyFilePath) {
        this.cloudName = cloudName;
        this.imageName = imageName;
        this.username = username;
        this.publicKey = publicKey;
        this.privateKeyFilePath = privateKeyFilePath;
        this.requirements = new HashMap<>();
    }

    public Specification(String cloudName, String imageName, String username, String publicKey, String privateKeyFilePath,
                         String userDataFile, String userDataType) {
        this(cloudName, imageName, username, publicKey, privateKeyFilePath);
        this.userDataFile = userDataFile;
        this.userDataType = userDataType;
    }

    public Specification(String cloudName, String imageName, String username, String publicKey, String privateKeyFilePath,
                         String userDataFile, String userDataType, String vCPU, String memory, String disk) {
        this(cloudName, imageName, username, publicKey, privateKeyFilePath, userDataFile, userDataType);
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

    public String getCloudName() {return this.cloudName; }

    public String getImageName() {
        return this.imageName;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPrivateKeyFilePath() {
        return this.privateKeyFilePath;
    }

    public String getPublicKey() {
        return this.publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getContextScript() {
        return this.contextScript;
    }

    public void setContextScript(String contextScript) {
        this.contextScript = contextScript;
    }

    public String getUserDataFile() {
        return this.userDataFile;
    }

    public void setUserDataFile(String userDataFile) {
        this.userDataFile = userDataFile;
    }

    public String getUserDataType() {
        return this.userDataType;
    }

    public void setUserDataType(String userDataType) {
        this.userDataType = userDataType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if((this.cloudName != null) && !this.cloudName.isEmpty()){
            sb.append("CloudName: " + this.cloudName);
        }
        sb.append(" Image: " + this.imageName);
        sb.append(" PublicKey: " + this.publicKey);
        if ((this.contextScript != null) && !this.contextScript.isEmpty()) {
            sb.append(LN + "ContextScript: " + contextScript);
        }
        if ((this.userDataFile != null) && !this.userDataFile.isEmpty()) {
            sb.append(LN + "UserDataFile:" + this.userDataFile);
        }
        if ((this.userDataType != null) && !this.userDataType.isEmpty()) {
            sb.append(LN + "UserDataType:" + this.userDataType);
        }
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
        Specification cloneSpec = new Specification(this.cloudName, this.imageName, this.username, this.publicKey, this.privateKeyFilePath,
                this.userDataFile, this.userDataType);
        cloneSpec.putAllRequirements(this.getAllRequirements());
        return cloneSpec;
    }

    public JSONObject toJSON() {
        try {
            JSONObject specification = new JSONObject();
            specification.put(CLOUD_NAME_STR, this.getCloudName());
            specification.put(IMAGE_STR, this.getImageName());
            specification.put(USERNAME_STR, this.getUsername());
            specification.put(PUBLIC_KEY_STR, this.getPublicKey());
            specification.put(PRIVATE_KEY_FILE_PATH_STR, this.getPrivateKeyFilePath());
            specification.put(CONTEXT_SCRIPT_STR, this.getContextScript());
            specification.put(USER_DATA_FILE_STR, this.getUserDataFile());
            specification.put(USER_DATA_TYPE_STR, this.getUserDataType());
            specification.put(REQUIREMENTS_MAP_STR, getAllRequirements().toString());
            return specification;
        } catch (JSONException e) {
            LOGGER.debug("Error while trying to create a JSON from Specification", e);
            return null;
        }
    }

    public static Specification fromJSON(JSONObject specJSON) {
        Specification specification = new Specification(specJSON.optString(CLOUD_NAME_STR), specJSON.optString(IMAGE_STR), specJSON.optString(USERNAME_STR),
                specJSON.optString(PUBLIC_KEY_STR), specJSON.optString(PRIVATE_KEY_FILE_PATH_STR),
                specJSON.optString(USER_DATA_FILE_STR), specJSON.optString(USER_DATA_TYPE_STR));
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
        result = prime * result + ((cloudName == null) ? 0 : cloudName.hashCode());
        result = prime * result + ((contextScript == null) ? 0 : contextScript.hashCode());
        result = prime * result + ((imageName == null) ? 0 : imageName.hashCode());
        result = prime * result + ((privateKeyFilePath == null) ? 0 : privateKeyFilePath.hashCode());
        result = prime * result + ((publicKey == null) ? 0 : publicKey.hashCode());
        result = prime * result + ((userDataFile == null) ? 0 : userDataFile.hashCode());
        result = prime * result + ((userDataType == null) ? 0 : userDataType.hashCode());
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
        if (cloudName == null) {
            if (other.cloudName != null)
                return false;
        } else if (!cloudName.equals(other.cloudName))
            return false;
        if (contextScript == null) {
            if (other.contextScript != null)
                return false;
        } else if (!contextScript.equals(other.contextScript))
            return false;
        if (imageName == null) {
            if (other.imageName != null)
                return false;
        } else if (!imageName.equals(other.imageName))
            return false;
        if (privateKeyFilePath == null) {
            if (other.privateKeyFilePath != null)
                return false;
        } else if (!privateKeyFilePath.equals(other.privateKeyFilePath))
            return false;
        if (publicKey == null) {
            if (other.publicKey != null)
                return false;
        } else if (!publicKey.equals(other.publicKey))
            return false;
        if (userDataFile == null) {
            if (other.userDataFile != null)
                return false;
        } else if (!userDataFile.equals(other.userDataFile))
            return false;
        if (userDataType == null) {
            if (other.userDataType != null)
                return false;
        } else if (!userDataType.equals(other.userDataType))
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

