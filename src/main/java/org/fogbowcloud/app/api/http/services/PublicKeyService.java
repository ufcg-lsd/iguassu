package org.fogbowcloud.app.api.http.services;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.core.ApplicationFacade;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Lazy
@Component
public class PublicKeyService {

    private static final Logger logger = Logger.getLogger(PublicKeyService.class);

    private ApplicationFacade applicationFacade = ApplicationFacade.getInstance();

    public String getPublicKey() throws Exception {
        return applicationFacade.getPublicKeyFromProviderService();
    }
}
