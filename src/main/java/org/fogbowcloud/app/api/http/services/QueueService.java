package org.fogbowcloud.app.api.http.services;

import org.fogbowcloud.app.api.dtos.QueueDTORequest;
import org.fogbowcloud.app.api.dtos.QueueDTOResponse;
import org.fogbowcloud.app.api.dtos.ResourceNode;
import org.fogbowcloud.app.api.dtos.ResourceDTOResponse;
import org.fogbowcloud.app.core.ApplicationFacade;
import org.fogbowcloud.app.core.exceptions.UnauthorizedRequestException;
import org.fogbowcloud.app.core.models.user.User;
import org.fogbowcloud.app.jes.arrebol.dtos.QueueDTO;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Lazy
@Service
public class QueueService {

    private ApplicationFacade applicationFacade = ApplicationFacade.getInstance();

    public String createQueue(User user, QueueDTORequest queue) {
        return this.applicationFacade.createQueue(user, queue);
    }

    public List<QueueDTO> getQueues(User user) {
        return this.applicationFacade.getQueues(user);
    }

    public ResourceDTOResponse addNode(User user, String queueId, ResourceNode node) throws UnauthorizedRequestException {
        return this.applicationFacade.addNode(user, queueId, node);
    }

    public ResourceDTOResponse getNodes(User user, String queueId) throws UnauthorizedRequestException {

        return this.applicationFacade.getNodes(user, queueId);
    }

    public QueueDTOResponse getQueue(User user, String queueId) throws UnauthorizedRequestException {
        return new QueueDTOResponse(this.applicationFacade.getQueue(user, queueId));
    }
}
