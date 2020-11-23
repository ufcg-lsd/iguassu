package org.fogbowcloud.app.api.http.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.fogbowcloud.app.api.constants.Documentation;
import org.fogbowcloud.app.core.ApplicationFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = Documentation.Endpoint.NONCE)
@Api(value = Documentation.Nonce.DESCRIPTION, tags = Documentation.Nonce.TAG)
public class NonceAPI {

    private ApplicationFacade applicationFacade = ApplicationFacade.getInstance();

    @GetMapping
    @ApiOperation(value = Documentation.Nonce.GENERATE, tags = Documentation.Nonce.TAG, response = String.class)
    public ResponseEntity<String> getNonce() {
        return new ResponseEntity<>(String.valueOf(this.applicationFacade.getNonce()), HttpStatus.OK);
    }
}
