package org.fogbowcloud.app.jes.arrebol.models;

import java.util.List;
import java.util.Map;

public class ArrebolTaskSpec {

    private Long id;

    private List<ArrebolCommand> commands;

    private Map<String, Object> metadata;

    public ArrebolTaskSpec(Long id){
        this.id = id;
    }

    public Long getId(){
        return id;
    }

    public List<ArrebolCommand> getCommands() {
        return commands;
    }
}

