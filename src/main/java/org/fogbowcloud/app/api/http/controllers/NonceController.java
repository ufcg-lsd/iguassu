package org.fogbowcloud.app.api.http.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.fogbowcloud.app.core.IguassuController;
import org.fogbowcloud.app.api.constants.ApiDocumentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping(value = NonceController.NONCE_ENDPOINT)
@Api(description = ApiDocumentation.Nonce.API)
public class NonceController {

    public static final String NONCE_ENDPOINT = "nonce";

    @Lazy
    @Autowired
    IguassuController iguassuController;

    @RequestMapping(method = RequestMethod.GET)
    @ApiParam(value = ApiDocumentation.Nonce.GET_OPERATION)
    public ResponseEntity<String> getNonce() {
        int nonce = this.iguassuController.getNonce();
        String nonceStr = String.valueOf(nonce);
        return new ResponseEntity<String>(nonceStr, HttpStatus.OK);
    }
}
