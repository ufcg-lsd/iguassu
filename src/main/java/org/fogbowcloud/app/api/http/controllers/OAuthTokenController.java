package org.fogbowcloud.app.api.http.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.api.constants.ApiDocumentation;
import org.fogbowcloud.app.api.http.services.OAuthService;
import org.fogbowcloud.app.core.constants.IguassuPropertiesConstants;
import org.fogbowcloud.app.core.exceptions.InvalidParameterException;
import org.fogbowcloud.app.core.datastore.OAuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = ApiDocumentation.ApiEndpoints.OAUTH_TOKEN_ENDPOINT)
@Api(ApiDocumentation.OAuthToken.API)
public class OAuthTokenController {
    private final Logger LOGGER = Logger.getLogger(OAuthTokenController.class);

    @Lazy
    private OAuthService oAuthService;

    @Autowired
    public OAuthTokenController(OAuthService oAuthService) {
        this.oAuthService = oAuthService;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(value = ApiDocumentation.OAuthToken.REQUEST_ACCESS_TOKEN)
    public ResponseEntity requestAccessToken(
            @ApiParam(value = ApiDocumentation.OAuthToken.REQUEST_ACCESS_TOKEN_BODY_MSG)
            @RequestBody String authorizationCode,
            @ApiParam(value = ApiDocumentation.CommonParameters.OAUTH_CREDENTIALS)
            @RequestHeader(value = IguassuPropertiesConstants.X_IDENTIFIERS) String applicationIdentifiers) {

        try {
            if (authorizationCode != null && !authorizationCode.trim().isEmpty()) {

                final OAuthToken oAuthToken = this.oAuthService.requestAccessToken(authorizationCode, applicationIdentifiers);

                return new ResponseEntity<>(oAuthToken, HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>("The authorization code is invalid.",
                        HttpStatus.BAD_REQUEST);
            }
        }
        catch (Exception e){
            return new ResponseEntity<>("The authorization failed with error [" + e.getMessage() +
                    "]", HttpStatus.INTERNAL_SERVER_ERROR);
        }
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

    @RequestMapping(method = RequestMethod.DELETE)
    @ApiOperation(value = ApiDocumentation.OAuthToken.DELETE_OPERATION)
    public ResponseEntity<List<OAuthToken>> deleteAllOAuthTokens() {
        LOGGER.info("Deleting all OAuth tokens.");

        this.oAuthService.deleteAllTokens();
        return new ResponseEntity<List<OAuthToken>>(HttpStatus.ACCEPTED);
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
