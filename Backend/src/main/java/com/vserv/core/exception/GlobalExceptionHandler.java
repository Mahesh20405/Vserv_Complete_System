package com.vserv.core.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiError> handleBusiness(BusinessException ex) {
		String message = ex.getMessage() != null ? ex.getMessage() : "Request failed.";
		if (ex instanceof NotFoundException) {
			return error(HttpStatus.NOT_FOUND, "Not Found", message);
		}
		if (ex instanceof ForbiddenException) {
			return error(HttpStatus.FORBIDDEN, "Forbidden", message);
		}
		if (ex instanceof ConflictException) {
			return error(HttpStatus.CONFLICT, "Conflict", message);
		}
		return error(HttpStatus.BAD_REQUEST, "Bad Request", message);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex) {
		return error(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
		return error(HttpStatus.FORBIDDEN, "Forbidden", "Access denied.");
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex) {
		return error(HttpStatus.UNAUTHORIZED, "Unauthorized", "Invalid email or password.");
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream().findFirst()
				.map(error -> error.getField() + ": " + error.getDefaultMessage()).orElse("Validation failed.");
		return validationError(message);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		String field = ex.getName() != null ? ex.getName() : "parameter";
		return validationError(field + ": Invalid value.");
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex) {
		return validationError("Malformed request body.");
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
		return validationError(ex.getMessage() != null ? ex.getMessage() : "Invalid request value.");
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex) {
		return error(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleGeneric(Exception ex) {
		log.error("Unhandled exception", ex);
		return internalServerError();
	}

	private ResponseEntity<ApiError> validationError(String message) {
		return error(HttpStatus.BAD_REQUEST, "Validation Failed", message);
	}

	private ResponseEntity<ApiError> internalServerError() {
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred.");
	}

	private ResponseEntity<ApiError> error(HttpStatus status, String error, String message) {
		return ResponseEntity.status(status).body(new ApiError(status.value(), error, message));
	}
}
