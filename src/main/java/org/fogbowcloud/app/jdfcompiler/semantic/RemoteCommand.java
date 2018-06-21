package org.fogbowcloud.app.jdfcompiler.semantic;

public class RemoteCommand extends JDLCommand{

	private String content;

	public RemoteCommand(String content) {
		super();
		this.setBlockType(JDLCommandType.REMOTE);
		this.content = content;
	}
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
