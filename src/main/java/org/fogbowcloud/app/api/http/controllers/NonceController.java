package org.fogbowcloud.app.api.http.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.fogbowcloud.app.api.constants.ApiDocumentation;
import org.fogbowcloud.app.core.IguassuFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = ApiDocumentation.Endpoint.NONCE_ENDPOINT)
@Api(ApiDocumentation.Nonce.API_DESCRIPTION)
public class NonceController {

    @Lazy
    @Autowired
    private IguassuFacade iguassuFacade;

    @GetMapping
    @ApiOperation(value = ApiDocumentation.Nonce.GET_OPERATION)
    public ResponseEntity<String> getNonce() {
        return new ResponseEntity<>(String.valueOf(this.iguassuFacade.getNonce()),
                HttpStatus.OK);
    }
}
