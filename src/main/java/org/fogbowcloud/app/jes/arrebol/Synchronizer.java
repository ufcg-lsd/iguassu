package org.fogbowcloud.app.jes.arrebol;

import org.fogbowcloud.app.jdfcompiler.job.JDFJob;

public interface Synchronizer {
    //TODO Review method name
    JDFJob sync(JDFJob job);
}
