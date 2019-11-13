package org.fogbowcloud.app.api.http.services;

import org.fogbowcloud.app.api.dtos.QueueRequest;
import org.fogbowcloud.app.core.ApplicationFacade;
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
}
