package org.fogbowcloud.app.jes.arrebol;

import org.fogbowcloud.app.jdfcompiler.job.JDFJob;

public interface JobSynchronizer {
    //TODO Review method name
    public JDFJob synchronizeJob(JDFJob job);
}
