package org.fogbowcloud.app.core.models.arrebol;

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

