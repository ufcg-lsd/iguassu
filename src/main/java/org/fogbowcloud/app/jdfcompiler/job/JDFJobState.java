package org.fogbowcloud.app.jdfcompiler.job;

public enum JDFJobState {
    SUBMITTED("Submitted"),
    QUEUED("Queued"),
    FAILED("Failed"),
    CREATED("Created"),
    FINISHED("Finished"),
    RUNNING("Running");

    private String desc;

    JDFJobState(String desc) {
        this.desc = desc;
    }

    public static JDFJobState create(String desc) throws Exception {
        for (JDFJobState ts : values()) {
            if (ts.value().equals(desc)) {
                return ts;
            }
        }
        throw new Exception("Invalid task state!");
    }

    public String value() {
        return this.desc;
    }
}
