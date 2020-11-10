package org.fogbowcloud.app.api.http.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.fogbowcloud.app.api.constants.Documentation;
import org.fogbowcloud.app.api.constants.Documentation.Endpoint;
import org.fogbowcloud.app.api.http.services.PublicKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = Endpoint.PUBLIC_KEY)
@Api(value = Documentation.PublicKey.DESCRIPTION, tags = Documentation.PublicKey.TAG)
public class PublicKeyAPI {

    private final Logger logger = Logger.getLogger(PublicKeyAPI.class);

    @Lazy
    private PublicKeyService publicKeyService;

    @Autowired
    public PublicKeyAPI(PublicKeyService publicKeyService) {
        this.publicKeyService = publicKeyService;
    }

    @GetMapping
    @ApiOperation(value = Documentation.PublicKey.GET, tags = Documentation.PublicKey.TAG, response = String.class)
    public ResponseEntity<?> getPublicKey() {
        logger.info("Getting public key from Provider Service");
        String publicKey;
        try {
            publicKey = this.publicKeyService.getPublicKey();
            return new ResponseEntity<>(publicKey, HttpStatus.OK);
        } catch (Throwable t) {
            return ResponseEntity.badRequest()
                .body(String.format("Operation returned error: %s", t.getMessage()));
        }
    }
}
