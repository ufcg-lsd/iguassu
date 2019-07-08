package org.fogbowcloud.app.jdfcompiler.job;

import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.command.Command;
import org.fogbowcloud.app.core.constants.DockerConstants;
import org.fogbowcloud.app.core.datastore.OAuthTokenDataStore;
import org.fogbowcloud.app.core.task.Specification;
import org.fogbowcloud.app.core.task.Task;
import org.fogbowcloud.app.core.task.TaskImpl;
import org.fogbowcloud.app.jdfcompiler.main.CompilerException;
import org.fogbowcloud.app.jdfcompiler.semantic.IOCommand;
import org.fogbowcloud.app.jdfcompiler.semantic.JDLCommand;
import org.fogbowcloud.app.jdfcompiler.semantic.JDLCommand.JDLCommandType;
import org.fogbowcloud.app.jdfcompiler.semantic.RemoteCommand;
import org.fogbowcloud.app.core.constants.IguassuPropertiesConstants;
import org.springframework.http.HttpStatus;

public class JDFJobBuilder {
	private static final Logger LOGGER = Logger.getLogger(JDFJobBuilder.class);

	private final Properties properties;
	private OAuthTokenDataStore oAuthTokenDataStore;

	public JDFJobBuilder(Properties properties) {
		this.properties = properties;
	}

	public JDFJobBuilder(Properties properties, OAuthTokenDataStore oAuthTokenDataStore) {
		this(properties);
		this.oAuthTokenDataStore = oAuthTokenDataStore;
	}

	/**
	 * @param job Job being created
	 * @param jdfFilePath Path to the jdf file that describes the job
	 * @throws IllegalArgumentException If path to jdf is empty
	 * @throws CompilerException If file does not describe a jdf Job
	 * @throws IOException If a problem during the reading of the file occurs
	 */
	public void createJobFromJDFFile(JDFJob job, String jdfFilePath, JobSpecification jobSpec, String userName,
									 String externalOAuthToken)
			throws IOException, InterruptedException {
		if (jdfFilePath == null || jdfFilePath.isEmpty()) {
			throw new IllegalArgumentException("jdfFilePath cannot be null");
		}

		File file = new File(jdfFilePath);

		if (file.exists()) {
			if (file.canRead()) {

				if (!jobSpec.getLabel().trim().isEmpty()) {
					job.setFriendlyName(jobSpec.getLabel());
				}

				String jobRequirements = jobSpec.getRequirements();
				LOGGER.debug("JobReq: " + jobRequirements);

				jobRequirements = jobRequirements.replace("(", "").replace(")", "");

				String image = null;

				for (String req : jobRequirements.split("and")) {
					if (req.trim().startsWith("image")) {
						image = req.split("==")[1].trim();
					}
				}

				Specification spec = new Specification(image, userName);

				addAllRequirements(jobRequirements, spec);

				int taskID = 0;
				for (TaskSpecification taskSpec : jobSpec.getTaskSpecs()) {
					if (Thread.interrupted())
						throw new InterruptedException();

					String uuid = UUID.randomUUID().toString();
					Task task = new TaskImpl("TaskNumber" + "-" + taskID + "-" + uuid, spec, uuid);
					task.putMetadata(IguassuPropertiesConstants.JOB_ID, job.getId());
					task.putMetadata(IguassuPropertiesConstants.OWNER, job.getOwner());

					parseInitCommands(job.getId(), taskSpec, task, userName, externalOAuthToken);
					parseTaskCommands(job.getId(), taskSpec, task, userName, externalOAuthToken);
					parseFinalCommands(job.getId(), taskSpec, task, userName, externalOAuthToken);

					job.addTask(task);
					LOGGER.debug("Task spec:\n" + task.getSpecification().toString());

					taskID++;
				}
			} else {
				throw new IllegalArgumentException(
						"Unable to read file: " + file.getAbsolutePath() + " check your permissions."
				);
			}
		} else {
			throw new IllegalArgumentException("File: " + file.getAbsolutePath() + " does not exists.");
		}
	}

	private void addAllRequirements(String jobRequirements, Specification spec){
		for (String req : jobRequirements.split("and")) {
		    if(req != null && !req.isEmpty()){
            if(req.startsWith(DockerConstants.PREFIX_DOCKER_REQUIREMENTS)){
                String requirements = spec.getRequirementValue(DockerConstants.METADATA_DOCKER_REQUIREMENTS);
                if(requirements == null){
                    spec.addRequirement(DockerConstants.METADATA_DOCKER_REQUIREMENTS, req);
                } else {
                    spec.addRequirement(DockerConstants.METADATA_DOCKER_REQUIREMENTS, requirements + " && " + req);
                }
            }
        }
		}
	}

	/**
	 * This method translates the JDF remote executable command into the JDL
	 * format
	 *
	 * @param jobId ID of the Job
	 * @param taskSpec The task specification {@link TaskSpecification}
	 * @param task The output expression containing the JDL job
	 */
	private void parseTaskCommands(String jobId, TaskSpecification taskSpec, Task task, String userName,
								   String externalOAuthToken) {
		List<JDLCommand> initBlocks = taskSpec.getTaskBlocks();
		addCommands(initBlocks, jobId, task, userName, externalOAuthToken);
	}

	/**
	 * It translates the input IOBlocks to JDL Input
	 *
	 * @param jobId ID of the Job
	 * @param taskSpec The task specification {@link TaskSpecification}
	 * @param task The output expression containing the JDL job 
	 */
	private void parseInitCommands(String jobId, TaskSpecification taskSpec, Task task, String userName,
								   String externalOAuthToken) {
		List<JDLCommand> initBlocks = taskSpec.getInitBlocks();
		addCommands(initBlocks, jobId, task, userName, externalOAuthToken);
	}

	private void addCommands(List<JDLCommand> initBlocks, String jobId, Task task, String userName,
							 String externalOAuthToken) {
		if (initBlocks == null) {
			return;
		}
		for (JDLCommand jdlCommand : initBlocks) {
			if (jdlCommand.getBlockType().equals(JDLCommandType.IO)) {
				addIOCommands(jobId, task, (IOCommand) jdlCommand, userName, externalOAuthToken);
			} else {
				addRemoteCommand(jobId, task, (RemoteCommand) jdlCommand);
			}
		}
	}

	private void addIOCommands(String jobId, Task task, IOCommand command, String userName,
							   String externalOAuthToken) {
		String sourceFile = command.getEntry().getSourceFile();
		String destination = command.getEntry().getDestination();
		String IOType = command.getEntry().getCommand().toUpperCase();
    	String rawCommand = IOType + " " + sourceFile + " " + destination;
		switch (IOType) {
			case "PUT": case "STORE":
				Command uploadFileCommand = uploadFileCommands(sourceFile, destination, userName, externalOAuthToken, rawCommand);
				LOGGER.debug(rawCommand);

				task.addCommand(uploadFileCommand);
				LOGGER.debug("JobId: " + jobId + " task: " + task.getId() + " upload command:" + uploadFileCommand.getCommand());
				break;
			case "GET":
                Command downloadFileCommand = downloadFileCommands(sourceFile, destination, userName, externalOAuthToken, rawCommand);
                task.addCommand(downloadFileCommand);
				LOGGER.debug("JobId: " + jobId + " task: " + task.getId() + " download command:" + downloadFileCommand.getCommand());
				break;
		}
	}

	private void addRemoteCommand(String jobId, Task task, RemoteCommand remCommand) {
		String commandStr = remCommand.getContent();

		Command command = new Command(commandStr);
		LOGGER.debug("JobId: " + jobId + " task: " + task.getId() + " remote command: " + remCommand.getContent());
		task.addCommand(command);
	}

	/**
	 * This method translates the Ourgrid output IOBlocks to JDL Input
	 *
	 * @param jobId ID of the Job
	 * @param taskSpec The task specification {@link TaskSpecification}
	 * @param task The output expression containing the JDL job
	 */
	private void parseFinalCommands(String jobId, TaskSpecification taskSpec, Task task, String userName,
									String externalOAuthToken) {
		List<JDLCommand> initBlocks = taskSpec.getFinalBlocks();
		addCommands(initBlocks, jobId, task, userName, externalOAuthToken);
	}

	private Command uploadFileCommands(String localFilePath, String filePathToUpload, String userName, String token, String rawCommand) {
		String fileDriverHostIp = this.properties.getProperty(IguassuPropertiesConstants.STORAGE_SERVICE_HOST);
		String requestTokenCommand = this.oAuthTokenDataStore.getAccessTokenByOwnerUsername(userName).get(0).getAccessToken();
		String uploadCommand = " http_code=$(curl --write-out %{http_code} -X PUT --header \"Authorization:Bearer \"$token "
				+ " --data-binary @" + localFilePath + " --silent --output /dev/null "
				+ " http://$server/remote.php/webdav/" + filePathToUpload + "); ";

		String scpCommand = "server=" + fileDriverHostIp + "; "
				+ "token=" + token + "; "
				+ uploadCommand
				+ " if [ \\$http_code == " + HttpStatus.UNAUTHORIZED + " ] ; then " + requestTokenCommand
				+ uploadCommand + " fi";

		return new Command(scpCommand, rawCommand);
	}

	private Command downloadFileCommands(String localFilePath, String filePathToDownload, String userName, String token, String rawCommand) {
		String fileDriverHostIp = this.properties.getProperty(IguassuPropertiesConstants.STORAGE_SERVICE_HOST);
		String requestTokenCommand = this.oAuthTokenDataStore.getAccessTokenByOwnerUsername(userName).get(0).getAccessToken();
		String downloadCommand = " full_response=$(curl --write-out %{http_code} --header \"Authorization:Bearer \"$token"
				+ " http://$server/remote.php/webdav/" + filePathToDownload
				+ " --silent --output " + localFilePath + " /dev/null); ";
		String extractHttpStatusCode = "http_code=${full_response:0:3}; ";

		String scpCommand = "server=" + fileDriverHostIp + "; "
				+ "token=" + token + "; "
				+ downloadCommand
				+ extractHttpStatusCode
				+ " if [ \\$http_code == " + HttpStatus.UNAUTHORIZED + " ] ; then " + requestTokenCommand
				+ " " + downloadCommand + " fi";

		return new Command(scpCommand, rawCommand);
	}

}
