package org.fogbowcloud.app.api.http.services;

import java.util.List;
import org.fogbowcloud.app.core.ApplicationFacade;
import org.fogbowcloud.app.core.models.user.User;
import org.fogbowcloud.app.jes.arrebol.dtos.QueueDTO;
import org.fogbowcloud.app.jes.arrebol.models.QueueSpec;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
public class QueueService {

    private ApplicationFacade applicationFacade = ApplicationFacade.getInstance();

    public String createQueue(User user, QueueSpec queueSpec) {
        return this.applicationFacade.createQueue(user, queueSpec);
    }

    public List<QueueDTO> getQueues(User user) {
        return this.applicationFacade.getQueues(user);
    }
}
