package org.fogbowcloud.app.core.dto;

import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.job.JDFJobState;

public class JobResponseDTO {

    private String id;
    private String name;
    private JDFJobState state;

    public JobResponseDTO(JDFJob job) {
        setFields(job);
    }

    public void setFields(JDFJob jdfJob) {
        this.id = jdfJob.getId();
        this.name = jdfJob.getName();
        this.state = jdfJob.getState();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JDFJobState getState() {
        return state;
    }

    public void setState(JDFJobState state) {
        this.state = state;
    }
}
