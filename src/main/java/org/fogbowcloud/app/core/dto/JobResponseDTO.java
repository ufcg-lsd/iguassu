package org.fogbowcloud.app.core.dto;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.fogbowcloud.app.jdfcompiler.job.JDFJob;
import org.fogbowcloud.app.jdfcompiler.job.JDFJobState;

public class JobResponseDTO {

    private String id;
    private String name;
    private String creationDate;
    private JDFJobState state;

    public JobResponseDTO(JDFJob job) {
        setFields(job);
    }

    private void setFields(JDFJob jdfJob) {
        this.id = jdfJob.getId();
        this.name = jdfJob.getName();
        this.creationDate = timestampToDate(jdfJob.getTimeStamp());
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

    public String getCreationDate(){
        return this.creationDate;
    }

    private String timestampToDate(long timestamp){
        Date date = new java.util.Date(timestamp*1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT-3"));
        return sdf.format(date);
    }
}
