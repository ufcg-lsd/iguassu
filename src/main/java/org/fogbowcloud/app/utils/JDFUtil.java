package org.fogbowcloud.app.utils;

import org.fogbowcloud.app.jdfcompiler.job.JobSpecification;
import org.fogbowcloud.app.jdfcompiler.job.TaskSpecification;
import org.fogbowcloud.app.jdfcompiler.semantic.JDLCommand;
import org.fogbowcloud.app.jdfcompiler.semantic.RemoteCommand;

/** Utility class for postprocessing compiled JDF commands. */
public class JDFUtil {

    public static void removeEmptySpaceFromVariables(JobSpecification jobSpec) {
        for (TaskSpecification taskSpec : jobSpec.getTaskSpecs()) {
            for (JDLCommand command : taskSpec.getTaskBlocks()) {
                if (command instanceof RemoteCommand) {
                    String content = ((RemoteCommand) command).getContent().replaceAll(" =", "=");
                    ((RemoteCommand) command).setContent(content);
                }
            }
        }
    }
}
