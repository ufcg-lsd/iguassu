package org.fogbowcloud.app.api.http.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.fogbowcloud.app.api.constants.Documentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = Documentation.Endpoint.VERSION)
@Api(Documentation.Version.DESCRIPTION)
public class VersionAPI {

    @GetMapping
    @ApiOperation(value = Documentation.Version.CURRENT_VERSION)
    public ResponseEntity<String> getNonce() {
        return new ResponseEntity<>("{\"version\": \"2.3.1\"}",
                HttpStatus.OK);
    }
}
