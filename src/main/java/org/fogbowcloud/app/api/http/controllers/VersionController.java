package org.fogbowcloud.app.api.http.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.fogbowcloud.app.api.constants.ApiDocumentation;
import org.fogbowcloud.app.api.constants.ApiDocumentation.Endpoint;
import org.fogbowcloud.app.api.constants.ApiDocumentation.Version;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = Endpoint.VERSION)
@Api(Version.DESCRIPTION)
public class VersionController {

    @GetMapping
    @ApiOperation(value = ApiDocumentation.Version.GET_OPERATION)
    public ResponseEntity<String> getNonce() {
        return new ResponseEntity<>("{\"version\": \"1.0.0\"}",HttpStatus.OK);
    }

}
