package org.fogbowcloud.app.api.http.services;

import org.fogbowcloud.app.api.dtos.NodeDTO;
import org.fogbowcloud.app.api.dtos.NodeRequest;
import org.fogbowcloud.app.api.dtos.QueueRequest;
import org.fogbowcloud.app.core.ApplicationFacade;
import org.fogbowcloud.app.core.exceptions.UnauthorizedRequestException;
import org.fogbowcloud.app.core.models.queue.ArrebolQueue;
import org.fogbowcloud.app.core.models.user.User;
import org.fogbowcloud.app.jes.arrebol.dtos.QueueDTO;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Lazy
@Service
public class QueueService {

    private ApplicationFacade applicationFacade = ApplicationFacade.getInstance();

    public String createQueue(User user, QueueRequest queue) {
        return this.applicationFacade.createQueue(user, queue);
    }

    public List<QueueDTO> getQueues(User user) {
        return this.applicationFacade.getQueues(user);
    }

    public NodeDTO addNode(User user, String queueId, NodeRequest node) throws UnauthorizedRequestException {
        return this.applicationFacade.addNode(user, queueId, node);
    }

    public NodeDTO getNodes(User user, String queueId) throws UnauthorizedRequestException {

        return this.applicationFacade.getNodes(user, queueId);
    }

    public ArrebolQueue getQueue(User user, String queueId) throws UnauthorizedRequestException {
        return this.applicationFacade.getQueue(user, queueId);
    }
}
