package org.fogbowcloud.app.jes.arrebol.models;

import java.util.List;

public class ArrebolTaskSpec {

    private String id;

    private List<ArrebolCommand> commands;

    public ArrebolTaskSpec(String id){
        this.id = id;
    }

    public String getId(){
        return id;
    }

    public List<ArrebolCommand> getCommands() {
        return commands;
    }
}

