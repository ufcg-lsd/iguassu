package org.fogbowcloud.app;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.constants.AppConstant;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.annotation.Bean;

import java.util.Properties;

@SpringBootApplication(exclude = RepositoryRestMvcAutoConfiguration.class)
public class IguassuApplication {

    private static final Logger LOGGER = Logger.getLogger(IguassuApplication.class);
    private static final String APPLICATION_PID_FILE = "./bin/shutdown.pid";

    public static void main(String[] args) {
        loadArguments(args);
        SpringApplication springApplication = new SpringApplication(IguassuApplication.class);
        springApplication.addListeners(new ApplicationPidFileWriter(APPLICATION_PID_FILE));
        springApplication.run(args);
    }

    private static void loadArguments(String[] args) {
        Options options = new Options();

        String opt = "c";
        String longOpt = AppConstant.CONF_FILE_PROPERTY;
        String description = "Configuration file path";
        Option confFilePath = new Option(opt, longOpt, true, description);
        confFilePath.setRequired(false);
        options.addOption(confFilePath);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption(longOpt)) {
                String inputFilePath = cmd.getOptionValue(longOpt);
                System.setProperty(AppConstant.CONF_FILE_PROPERTY, inputFilePath);
            }
        } catch (ParseException e) {
            LOGGER.error("Error while loading command line arguments: " + e.getMessage(), e);
            System.exit(1);
        }
    }

    @Bean
    CommandLineRunner cmdRunner() {
        return new IguassuMainRunner();
    }

    @Bean
    public Properties properties() {
        return new Properties();
    }

}