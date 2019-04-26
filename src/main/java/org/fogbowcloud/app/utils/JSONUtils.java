package org.fogbowcloud.app.utils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class JSONUtils {

    public static Map<String, String> toMap(String jsonStr) {
        Map<String, String> newMap = new HashMap<String, String>();
        jsonStr = jsonStr.replace("{", "").replace("}", "");
        String[] blocks = jsonStr.split(",");
        for (int i = 0; i < blocks.length; i++) {
            String block = blocks[i];
            int indexOfCarac = block.indexOf("=");
            if (indexOfCarac < 0) {
                continue;
            }
            String key = block.substring(0, indexOfCarac).trim();
            String value = block.substring(indexOfCarac + 1).trim();
            newMap.put(key, value);
        }
        return newMap;
    }

    public static String getValueFromJsonStr(String key, String jsonStr) {
        JSONObject json = new JSONObject(jsonStr);
        String value = json.getString(key);
        return value;
    }
}
