package org.fogbowcloud.app.api.http;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.model.OAuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Properties;

@CrossOrigin
@RestController
@RequestMapping(value = OAuthTokenController.OAUTH_TOKEN_ENDPOINT)
public class OAuthTokenController {

    public static final String OAUTH_TOKEN_ENDPOINT = "oauthtoken";

    private final Logger LOGGER = Logger.getLogger(OAuthTokenController.class);

    OAuthService oAuthService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<OAuthToken> storeOAuthToken(@RequestBody OAuthToken oAuthToken) {
        LOGGER.info("Saving new OAuth Token.");

        this.oAuthService.storeOAuthToken(oAuthToken);
        return new ResponseEntity<OAuthToken>(oAuthToken, HttpStatus.CREATED);
    }
    
    @Autowired
    public void setOAuthService(Properties properties) {
        this.oAuthService = new OAuthService(properties);
    }

}
