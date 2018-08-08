package org.fogbowcloud.app.api.http.controllers;

import org.fogbowcloud.app.ArrebolController;
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
public class NonceController {

    public static final String NONCE_ENDPOINT = "nonce";

    @Lazy
    @Autowired
    ArrebolController arrebolController;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> getNonce() {
        int nonce = this.arrebolController.getNonce();
        String nonceStr = String.valueOf(nonce);
        return new ResponseEntity<String>(nonceStr, HttpStatus.OK);
    }
}
