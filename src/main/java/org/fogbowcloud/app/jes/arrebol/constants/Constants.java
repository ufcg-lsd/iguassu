package org.fogbowcloud.app.jes.arrebol.constants;

public class Constants {

    public static class Endpoint {

        public static final String QUEUES = "%s/queues";
        public static final String QUEUE = QUEUES + "/%s";
        public static final String JOBS = QUEUE + "/jobs";
        public static final String JOB = JOBS + "/%s";
        public static final String WORKERS = QUEUE + "/workers";
    }

    public static class JsonField {
        public static class Node {
            public static final String ADDRESS = "address";
            public static final String POOL_SIZE = "pool_size";
        }
    }

}
