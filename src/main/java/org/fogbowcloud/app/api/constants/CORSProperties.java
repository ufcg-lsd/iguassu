package org.fogbowcloud.app.api.constants;

public class CORSProperties {

    public static final String ALLOWED_ORIGINS_HEADER = "Access-Control-Allow-Origin";
    public static final String MAX_AGE_HEADER = "Access-Control-Max-Age";
    public static final String MAX_AGE_HEADER_VALUE = "3600";
    public static final String ALLOWED_CREDENTIALS_HEADER = "Access-Control-Allow-Credentials";
    public static final String ALLOWED_CREDENTIALS_HEADER_VALUE = "true";
    public static final String ALLOWED_METHODS_HEADER = "Access-Control-Allow-Methods";
    public static final String ALLOWED_METHODS_HEADER_VALUE = "POST, PUT, GET, OPTIONS, DELETE";
    public static final String ALLOWED_HEADERS_HEADER = "Access-Control-Allow-Headers";
    public static final String ALLOWED_HEADERS_HEADER_VALUES =
            "Content-Type, "
                    + "Access-Control-Allow-Headers, "
                    + "Authorization, "
                    + "X-Requested-With, "
                    + "X-Auth-App-Identifiers, "
                    + "X-Auth-User-Credentials";
    public static final String ALLOWED_CLIENT_FRONT_LOCAL = "http://localhost:8082";
    public static final String VARY_HEADER = "Vary";
    public static final String VARY_HEADER_VALUE = "Origin";
}
