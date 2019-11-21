package org.fogbowcloud.app.ps;

public class Constants {
    public static final String POOL_NAME_KEY = "name";
    public static final String ERROR_MSG_KEY = "error";
    public static final String MSG_KEY = "msg";

    public static final class Endpoint {
        public static final String POOLS = "/pools";
        public static final String POOL = POOLS + "/%s";
    }

}
