package org.fogbowcloud.app.utils;

import java.io.File;
import java.util.Objects;

/** Utility class for database manipulation. */
public class DataStoreUtils {

    private static final String DATASTORES_FOLDER = "datastores";
    private static final String PREFIX_DATASTORE_URL = "jdbc:sqlite:";

    /**
     * @param dataStoreUrl coming from properties.
     * @param dataStoreName coming from each DataStore class.
     */
    public static String getDataStoreUrl(String dataStoreUrl, String dataStoreName) {
        if (Objects.isNull(dataStoreUrl) || dataStoreUrl.isEmpty()) {
            String projectAbsolutePath = System.getProperty("user.dir");
            dataStoreUrl =
                    PREFIX_DATASTORE_URL
                            + projectAbsolutePath
                            + "/"
                            + DATASTORES_FOLDER
                            + "/"
                            + dataStoreName;
            File datastoreDir = new File(DATASTORES_FOLDER);
            if (!datastoreDir.exists()) {
                datastoreDir.mkdirs();
            }
        }
        return dataStoreUrl;
    }
}
