package org.fogbowcloud.app.jdfcompiler.semantic;

public class JDLCommand {
	public enum JDLCommandType {
		IO, REMOTE,
	}

	private JDLCommandType blockType;
	
	public JDLCommand() {
	}

	public JDLCommandType getBlockType() {
		return blockType;
	}

	public void setBlockType(JDLCommandType blockType) {
		this.blockType = blockType;
	}
}
