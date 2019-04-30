package org.fogbowcloud.app.jdfcompiler.job;

import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.command.Command;
import org.fogbowcloud.app.core.constants.FogbowConstants;
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

	private static final String SSH_SCP_PRE_COMMAND = "-o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no";
	private static final String DEFAULT_FLAVOR_NAME = "default-compute-flavor";
	private static final String ENV_PRIVATE_KEY_FILE = "PRIVATE_KEY_FILE";
	private static final String REMOTE_HOST = "HOST";
	private static final String ENV_SSH_PORT = "SSH_PORT";
	private static final String ENV_SSH_USER = "SSH_USER";

	private final Properties properties;

	public JDFJobBuilder(Properties properties) {
		this.properties = properties;
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

				job.setFriendlyName(jobSpec.getLabel());				

				String jobRequirements = jobSpec.getRequirements();
				LOGGER.debug("JobReq: " + jobRequirements);

				jobRequirements = jobRequirements.replace("(", "").replace(")", "");

				String image = this.properties.getProperty(DEFAULT_FLAVOR_NAME);

				for (String req : jobRequirements.split("and")) {
					if (req.trim().startsWith("image")) {
						image = req.split("==")[1].trim();
					}
				}

				Specification spec = new Specification(image, userName);

				int i = 0;
				for (String req : jobRequirements.split("and")) {
					if (i == 0 && !req.trim().startsWith("image")) {
						i++;
						spec.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, req);
					} else if (!req.trim().startsWith("image")) {
						spec.addRequirement(
								FogbowConstants.METADATA_FOGBOW_REQUIREMENTS,
								spec.getRequirementValue(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS) + " && " + req
						);
					}
				}

				spec.addRequirement(FogbowConstants.METADATA_REQUEST_TYPE, "one-time");
				int taskID = 0;
				for (TaskSpecification taskSpec : jobSpec.getTaskSpecs()) {
					if (Thread.interrupted())
						throw new InterruptedException();
					ProcessBuilder ps = new ProcessBuilder("id","-u", job.getUserId());

					//From the DOC:  Initially, this property is false, meaning that the
					//standard output and error output of a subprocess are sent to two
					//separate streams
					ps.redirectErrorStream(true);

					Process pr = ps.start();

					BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
					try {
						pr.waitFor();
					} catch (InterruptedException e) {
						LOGGER.debug("could not finish the query on user UUID", e);
					}
					String inputLine;
					StringBuilder resultBuilder = new StringBuilder();
					while ((inputLine = in.readLine()) != null) {
				        resultBuilder.append(inputLine);
				    }
					String result = resultBuilder.toString();
					LOGGER.debug("Result of the Process Builder: " + result);

					String uuid = UUID.randomUUID().toString();
					Task task = new TaskImpl("TaskNumber" + "-" + taskID + "-" + uuid, spec, uuid);
					task.putMetadata(TaskImpl.METADATA_REMOTE_OUTPUT_FOLDER,
							this.properties.getProperty(IguassuPropertiesConstants.REMOTE_OUTPUT_FOLDER));
					
					task.putMetadata(TaskImpl.METADATA_REMOTE_COMMAND_EXIT_PATH,
							this.properties.getProperty(IguassuPropertiesConstants.REMOTE_OUTPUT_FOLDER) + "/exit");
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

		switch (IOType) {
			case "PUT": case "STORE":
				Command uploadFileCommand = uploadFileCommands(sourceFile, destination, userName, externalOAuthToken);
				task.addCommand(uploadFileCommand);
				LOGGER.debug("JobId: " + jobId + " task: " + task.getId() + " upload command:" + uploadFileCommand.getCommand());
				break;
			case "GET":
                Command downloadFileCommand = downloadFileCommands(sourceFile, destination, userName, externalOAuthToken);
                task.addCommand(downloadFileCommand);
				LOGGER.debug("JobId: " + jobId + " task: " + task.getId() + " download command:" + downloadFileCommand.getCommand());
				break;
		}
	}

	private void addRemoteCommand(String jobId, Task task, RemoteCommand remCommand) {
		String commandStr = remCommand.getContent();;

		Command command = new Command(commandStr);
		LOGGER.debug("JobId: " + jobId + " task: " + task.getId() + " remote command: " + remCommand.getContent());
		task.addCommand(command);
	}

	private String getDirectoryTree(String destination) {
		int lastDir = destination.lastIndexOf(File.separator);
		if (lastDir == -1 ) {
			return "";
		}
		return destination.substring(0, lastDir);
	}

	private Command mkdirRemoteFolder(String folder) {
		if (folder.equals("")) {
			return null;
		}
		String mkdirCommand = "mkdir -p " + folder;
		return new Command(mkdirCommand);
	}

	private Command stageInCommand(String localFile, String remoteFile) {
		String scpCommand = "scp " + SSH_SCP_PRE_COMMAND + " -P $" + ENV_SSH_PORT
				+ " -i $" + ENV_PRIVATE_KEY_FILE + " " + localFile + " $"
				+ ENV_SSH_USER + "@" + "$" + REMOTE_HOST + ":" + remoteFile;
		return new Command(scpCommand);
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

	private Command stageOutCommand(String remoteFile, String localFile) {
		String scpCommand = "scp " + SSH_SCP_PRE_COMMAND + " -P $" + ENV_SSH_PORT
				+ " -i $" + ENV_PRIVATE_KEY_FILE + " $" + ENV_SSH_USER + "@" + "$"
				+ REMOTE_HOST + ": " + remoteFile + " " + localFile;
		return new Command(scpCommand);
	}

	private Command uploadFileCommands(String localFilePath, String filePathToUpload, String userName, String token) {
		String fileDriverHostIp = this.properties.getProperty(IguassuPropertiesConstants.STORAGE_SERVICE_HOST);
		String requestTokenCommand = getUserExternalOAuthTokenRequestCommand(userName);
		String uploadCommand = " http_code=$(curl --write-out %{http_code} -X PUT --header \"Authorization:Bearer $token\" "
				+ " --data-binary @" + localFilePath + " --silent --output /dev/null "
				+ " http://$server/remote.php/webdav/" + filePathToUpload + "); ";

		String scpCommand = "\'server=" + fileDriverHostIp + "; "
				+ "token=" + token + "; "
				+ uploadCommand
				+ " if [ \\$http_code == " + HttpStatus.UNAUTHORIZED + " ] ; then " + requestTokenCommand
				+ uploadCommand + " fi \'";

		return new Command(scpCommand);
	}

	private Command downloadFileCommands(String localFilePath, String filePathToDownload, String userName, String token) {
		String fileDriverHostIp = this.properties.getProperty(IguassuPropertiesConstants.STORAGE_SERVICE_HOST);
		String requestTokenCommand = getUserExternalOAuthTokenRequestCommand(userName);
		String downloadCommand = " full_response=$(curl --write-out %{http_code} --header \"Authorization:Bearer $token\" "
				+ " http://$server/remote.php/webdav/" + filePathToDownload
				+ " --silent --output " + localFilePath + " /dev/null); ";
		String extractHttpStatusCode = "http_code=${full_response:0:3}; ";

		String scpCommand = "\'server=" + fileDriverHostIp + "; "
				+ "token=" + token + "; "
				+ downloadCommand
				+ extractHttpStatusCode
				+ " if [ \\$http_code == " + String.valueOf(HttpStatus.UNAUTHORIZED) + " ] ; then " + requestTokenCommand
				+ " " + downloadCommand + " fi \'";

		return new Command(scpCommand);
	}

	private Command mkdirLocalFolder(String folder) {
		if (folder.equals("")) {
			return null;
		}
		String mkdirCommand = "su $UserID ; " + "mkdir -p " + folder;
		return new Command(mkdirCommand);
	}

	private String getUserExternalOAuthTokenRequestCommand(String userName) {
		String myIguassuHttpServiceIp = getIguassuHost();
		return "token=$(curl --request GET --url " + myIguassuHttpServiceIp + "/oauthtoken/" + userName + "); ";
	}

	private String getIguassuHost() {
		return "https://" + this.properties.getProperty(IguassuPropertiesConstants.IGUASSU_SERVICE_HOST) + "/api/v1";
	}
}