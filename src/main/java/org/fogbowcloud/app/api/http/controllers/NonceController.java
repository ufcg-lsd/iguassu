package org.fogbowcloud.app.api.http.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.fogbowcloud.app.api.constants.Documentation;
import org.fogbowcloud.app.core.IguassuFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = Documentation.Endpoint.NONCE)
@Api(Documentation.Nonce.DESCRIPTION)
public class NonceController {

	@Lazy @Autowired private IguassuFacade iguassuFacade;

	@GetMapping
	@ApiOperation(value = Documentation.Nonce.GET_OPERATION)
	public ResponseEntity<String> getNonce() {
		return new ResponseEntity<>(String.valueOf(this.iguassuFacade.getNonce()), HttpStatus.OK);
	}
}
