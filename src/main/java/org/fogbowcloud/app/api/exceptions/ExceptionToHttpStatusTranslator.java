package org.fogbowcloud.app.api.exceptions;

import org.fogbowcloud.app.core.exceptions.ExceptionResponse;
import org.fogbowcloud.app.core.exceptions.IguassuException;
import org.fogbowcloud.app.core.exceptions.InvalidParameterException;
import org.fogbowcloud.app.core.exceptions.UnauthorizedRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ExceptionToHttpStatusTranslator extends ResponseEntityExceptionHandler {

	@ExceptionHandler(UnauthorizedRequestException.class)
	public final ResponseEntity<ExceptionResponse> handleAuthorizationException(
		Exception ex, WebRequest request) {

		ExceptionResponse errorDetails =
			new ExceptionResponse(ex.getMessage(), request.getDescription(false));

		return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(InvalidParameterException.class)
	public final ResponseEntity<ExceptionResponse> handleInvalidParameterException(
		Exception ex, WebRequest request) {

		ExceptionResponse errorDetails =
			new ExceptionResponse(ex.getMessage(), request.getDescription(false));

		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(StorageException.class)
	public final ResponseEntity<ExceptionResponse> handleBadJobSubmitted(
		Exception ex, WebRequest request) {

		ExceptionResponse errorDetails =
			new ExceptionResponse(ex.getMessage(), request.getDescription(false));

		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(IguassuException.class)
	public final ResponseEntity<ExceptionResponse> handleAnyException(
		Exception ex, WebRequest request) {

		ExceptionResponse errorDetails =
			new ExceptionResponse(ex.getMessage(), request.getDescription(false));

		return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
