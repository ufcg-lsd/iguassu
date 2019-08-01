package org.fogbowcloud.app.utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/** Utility class for JSON manipulation. */
public class JSONUtils {

    public static Map<String, String> toMap(String jsonStr) {
        Map<String, String> newMap = new HashMap<String, String>();
        jsonStr = jsonStr.replace("{", "").replace("}", "");
        String[] blocks = jsonStr.split(",");
        for (String block : blocks) {
            int indexOfChar = block.indexOf("=");
            if (indexOfChar < 0) {
                continue;
            }
            String key = block.substring(0, indexOfChar).trim();
            String value = block.substring(indexOfChar + 1).trim();
            newMap.put(key, value);
        }
        return newMap;
    }

    public static String getValueFromJsonStr(String key, String jsonStr) {
        JSONObject json = new JSONObject(jsonStr);
        return json.getString(key);
    }
}
