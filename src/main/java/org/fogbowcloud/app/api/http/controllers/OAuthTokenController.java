package org.fogbowcloud.app.api.http.controllers;

import org.apache.log4j.Logger;
import org.fogbowcloud.app.api.http.services.OAuthService;
import org.fogbowcloud.app.exception.InvalidParameterException;
import org.fogbowcloud.app.model.OAuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = OAuthTokenController.OAUTH_TOKEN_ENDPOINT)
public class OAuthTokenController {

    public static final String OAUTH_TOKEN_ENDPOINT = "oauthtoken";

    private final Logger LOGGER = Logger.getLogger(OAuthTokenController.class);

    @Lazy
    OAuthService oAuthService;

    @Autowired
    public OAuthTokenController(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<OAuthToken> storeOAuthToken(@RequestBody OAuthToken oAuthToken) {
        LOGGER.info("Saving new OAuth Token.");

        this.oAuthService.storeOAuthToken(oAuthToken);
        return new ResponseEntity<OAuthToken>(oAuthToken, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{ownerUsername}", method = RequestMethod.GET)
    public ResponseEntity<OAuthTokenResponse> getAccessTokenBy(@PathVariable String ownerUsername) throws InvalidParameterException {
        LOGGER.info("Retrieving OAuth token from user [" + ownerUsername + "]");

        String accessToken = this.oAuthService.getAccessTokenByOwnerUsername(ownerUsername);
        OAuthTokenResponse dataResponse = new OAuthTokenResponse(accessToken);
        return new ResponseEntity<>(dataResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<OAuthToken>> getAllOAuthTokens() {
        LOGGER.info("Retrieving all OAuth tokens.");

        List<OAuthToken> list = this.oAuthService.getAll();
        return new ResponseEntity<List<OAuthToken>>(list, HttpStatus.CREATED);
    }

    // TODO: delete this endpoints after tests
    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity deleteAllOAuthTokens() {
        LOGGER.info("Deleting all OAuth tokens.");

        this.oAuthService.deleteAllTokens();
        return new ResponseEntity<List<OAuthToken>>(HttpStatus.CREATED);
    }

    public class OAuthTokenResponse {
        private String token;
        public OAuthTokenResponse() {}
        public OAuthTokenResponse(String token) {
            this.token = token;
        }
        public String getToken() {
            return this.token;
        }
        public void setToken(String data) {
            this.token = data;
        }
    }

}
