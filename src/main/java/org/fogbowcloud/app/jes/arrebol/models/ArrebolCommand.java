package org.fogbowcloud.app.jes.arrebol.models;

public class ArrebolCommand {
    private String command;
    private ArrebolCommandState state;
    private Integer exitcode;

    public String getCommand() {
        return command;
    }

    public ArrebolCommandState getState() {
        return state;
    }

    public Integer getExitcode() {
        return exitcode;
    }
}
