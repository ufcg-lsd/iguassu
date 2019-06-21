package org.fogbowcloud.app.jes.arrebol;

import org.fogbowcloud.app.jdfcompiler.job.JDFJob;

public interface JobSynchronizer {
    //TODO Review method name
    JDFJob synchronizeJob(JDFJob job);
}
