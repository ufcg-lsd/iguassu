package org.fogbowcloud.app;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.restlet.JDFSchedulerApplication;
import org.fogbowcloud.blowout.core.exception.BlowoutException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ArrebolMain {

    public static final Logger LOGGER = Logger.getLogger(ArrebolMain.class);

    /**
     * This method receives a JDF file as input and requests the mapping of its
     * attributes to JDL attributes, generating a JDL file at the end
     *
     * @param args Path to the config files
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println(
                    "Incomplete arguments. Necessary to pass two args. (1) arrebol.conf path and (2) blowout.conf path.");
            System.exit(1);
        }

        Properties properties = new Properties();

        String arrebolConfPath = args[0];
        String schedConfPath = args[1];

        try {
            properties.load(new FileInputStream(arrebolConfPath));
            properties.load(new FileInputStream(schedConfPath));
        } catch (IOException e) {
            LOGGER.error("Failed to read configuration file.", e);
            System.exit(1);
        }

        try {
            final JDFSchedulerApplication app = new JDFSchedulerApplication(
                    new ArrebolController(properties)
            );
            final Thread mainThread = Thread.currentThread();
            Runtime.getRuntime().addShutdownHook(
                    new Thread() {
                        @Override
                        public void run() {
                            System.out.println("Exiting server");
                            try {
                                app.stopServer();
                                mainThread.join();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
            );
            app.startServer();
        } catch (BlowoutException e) {
            LOGGER.error("Failed to initialize Blowout.", e);
            System.exit(1);
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Arrebol.", e);
            System.exit(1);
        }
    }
}