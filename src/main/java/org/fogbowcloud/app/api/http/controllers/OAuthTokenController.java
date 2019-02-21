package org.fogbowcloud.app.api.http.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.api.config.ApiDocumentation;
import org.fogbowcloud.app.api.http.services.OAuthService;
import org.fogbowcloud.app.core.exceptions.InvalidParameterException;
import org.fogbowcloud.app.core.models.OAuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = OAuthTokenController.OAUTH_TOKEN_ENDPOINT)
@Api(description = ApiDocumentation.OAuthToken.API)
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
    @ApiOperation(value = ApiDocumentation.OAuthToken.STORE_OPERATION)
    public ResponseEntity<OAuthToken> storeOAuthToken(
            @ApiParam(value = ApiDocumentation.OAuthToken.CREATE_REQUEST_BODY)
            @RequestBody OAuthToken oAuthToken) {
        LOGGER.info("Saving new OAuth Token.");

        this.oAuthService.storeOAuthToken(oAuthToken);
        return new ResponseEntity<OAuthToken>(oAuthToken, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/{ownerUsername}", method = RequestMethod.GET)
    @ApiOperation(value = ApiDocumentation.OAuthToken.GET_BY_USER)
    public ResponseEntity<OAuthTokenResponse> getAccessTokenBy(
            @ApiParam(value = ApiDocumentation.OAuthToken.USER_NAME)
            @PathVariable String ownerUsername) throws InvalidParameterException {
        LOGGER.info("Retrieving OAuth token from user [" + ownerUsername + "]");

        String accessToken = this.oAuthService.getAccessTokenByOwnerUsername(ownerUsername);
        OAuthTokenResponse dataResponse = new OAuthTokenResponse(accessToken);
        return new ResponseEntity<>(dataResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = ApiDocumentation.OAuthToken.GET_OPERATION)
    public ResponseEntity<List<OAuthToken>> getAllOAuthTokens() {
        LOGGER.info("Retrieving all OAuth tokens.");

        List<OAuthToken> list = this.oAuthService.getAll();
        return new ResponseEntity<List<OAuthToken>>(list, HttpStatus.CREATED);
    }

    // TODO: delete this endpoints after tests
    @RequestMapping(method = RequestMethod.DELETE)
    @ApiOperation(value = ApiDocumentation.OAuthToken.DELETE_OPERATION)
    public ResponseEntity deleteAllOAuthTokens() {
        LOGGER.info("Deleting all OAuth tokens.");

        this.oAuthService.deleteAllTokens();
        return new ResponseEntity<List<OAuthToken>>(HttpStatus.CREATED);
    }

    public class OAuthTokenResponse {
        private String token;
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
