package org.fogbowcloud.app.api.http.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.api.constants.Documentation;
import org.fogbowcloud.app.api.dtos.UserDTO;
import org.fogbowcloud.app.api.http.services.AuthService;
import org.fogbowcloud.app.core.constants.AppConstant;
import org.fogbowcloud.app.core.exceptions.StorageServiceConnectException;
import org.fogbowcloud.app.core.models.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.GeneralSecurityException;
import java.util.Objects;

@RestController
@RequestMapping(value = Documentation.Endpoint.AUTH)
@Api(Documentation.Auth.DESCRIPTION)
public class AuthAPI {

    private final Logger logger = Logger.getLogger(AuthAPI.class);

    @Lazy
    private AuthService authService;

    @Autowired
    public AuthAPI(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(value = Documentation.Endpoint.OAUTH_2)
    @ApiOperation(value = Documentation.Auth.AUTHENTICATE_USER)
    public ResponseEntity<?> authenticate(
            @ApiParam(value = Documentation.Auth.AUTHORIZATION_CODE) @RequestBody
                    String authorizationCode,
            @ApiParam(value = Documentation.CommonParameters.OAUTH_CREDENTIALS)
            @RequestHeader(value = AppConstant.X_AUTH_APP_IDENTIFIERS)
                    String applicationIdentifiers) {
        logger.info("Authentication request received.");
        try {
            if (Objects.nonNull(authorizationCode) && !authorizationCode.trim().isEmpty()) {
                if (Objects.nonNull(applicationIdentifiers) && !applicationIdentifiers.trim().isEmpty()) {
                    try {
                        final User userAuthenticated = this.authService.authenticate(
                                authorizationCode, applicationIdentifiers);

                        return new ResponseEntity<>(new UserDTO(userAuthenticated), HttpStatus.OK);
                    } catch (GeneralSecurityException gse) {
                        return new ResponseEntity<>(
                                "The authentication failed with error [" + gse.getMessage() + "]",
                                HttpStatus.UNAUTHORIZED);
                    }
                } else {
                    return new ResponseEntity<>(
                            "Application identifiers are invalid. Check and try again.", HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>(
                        "The authorization code is invalid. Check and try again.", HttpStatus.BAD_REQUEST);
            }
        } catch (StorageServiceConnectException ssce) {
            return new ResponseEntity<>(
                    "Storage Service connection failed with error [" + ssce.getMessage() + "]",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Throwable throwable) {
            return new ResponseEntity<>(
                    "The authentication failed with error [" + throwable.getMessage() + "]",
                    HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping(Documentation.Endpoint.REFRESH_TOKEN_VERSION)
    public ResponseEntity<?> refreshToken(
            @PathVariable String userId, @PathVariable Long tokenVersion) {
        logger.info("Refresh OAuth2 tokens request received.");
        try {
            String refreshedAccessToken = this.authService.refreshToken(userId, tokenVersion);
            return new ResponseEntity<>(refreshedAccessToken, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
