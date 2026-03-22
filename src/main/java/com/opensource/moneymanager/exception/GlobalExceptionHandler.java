package com.opensource.moneymanager.exception;

import com.opensource.moneymanager.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.stream.Collectors;

/**
 * Global exception handler for the REST API.
 *
 * Architecture Overview:
 * - Uses @RestControllerAdvice to centralize exception handling across all controllers
 * - Provides consistent error response format (ErrorResponse DTO) for all exceptions
 * - Eliminates the need for try-catch blocks in individual controllers
 * - Logs all exceptions for monitoring and debugging purposes
 *
 * Key Benefits:
 * - Single point of control for error handling across the application
 * - Consistent HTTP status codes and error message formats
 * - Automatic conversion of different exception types to user-friendly responses
 * - Centralized logging for all error events
 *
 * Exception Handling Flow:
 * 1. An exception is thrown from any controller or service layer
 * 2. Spring catches the exception and routes it to the appropriate @ExceptionHandler method
 * 3. The handler method processes the exception and returns an ErrorResponse
 * 4. The ErrorResponse is serialized as JSON and returned to the client
 * 5. The exception is logged for monitoring and debugging
 *
 * @author Money Manager Team
 * @version 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors from @Valid annotation.
     *
     * This method is triggered when:
     * - Request body fields fail JSR-303 validation constraints (@NotNull, @NotBlank, etc.)
     * - Method argument validation fails during request binding
     *
     * Behavior:
     * - Extracts all field validation errors from the BindingResult
     * - Combines all error messages into a single string (separated by semicolons)
     * - Returns HTTP 400 Bad Request with detailed validation error information
     * - Logs the validation error at WARN level for monitoring
     *
     * Example Response:
     * {
     *   "status": 400,
     *   "message": "name: must not be blank; type: must not be null",
     *   "errorType": "Validation Error",
     *   "path": "/api/accounts"
     * }
     *
     * @param ex the MethodArgumentNotValidException containing binding errors
     * @param request the current web request context
     * @return ResponseEntity with ErrorResponse and HTTP 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        logger.warn("Validation error in request body");

        // Extract all field validation errors and create a detailed message
        String detailMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        // Create error response with validation details
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                detailMessage.isEmpty() ? "Request validation failed" : detailMessage,
                "Validation Error",
                getPath(request)
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle IllegalArgumentException for business logic validation errors.
     *
     * This method is triggered when:
     * - Service layer throws IllegalArgumentException for business rule violations
     * - Examples: duplicate account names, invalid account types, balance constraints
     * - Custom validation failures in business logic
     *
     * Behavior:
     * - Returns HTTP 400 Bad Request with the exception message as the error detail
     * - Uses "Validation Error" as the error type
     * - Logs the error at WARN level
     * - Suitable for recoverable, user-facing errors
     *
     * Example Response:
     * {
     *   "status": 400,
     *   "message": "Account with name 'Checking' already exists",
     *   "errorType": "Validation Error",
     *   "path": "/api/accounts"
     * }
     *
     * @param ex the IllegalArgumentException with business error details
     * @param request the current web request context
     * @return ResponseEntity with ErrorResponse and HTTP 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {
        logger.warn("Validation error: {}", ex.getMessage());

        // Create error response with the exception message
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                "Validation Error",
                getPath(request)
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle all other unexpected exceptions.
     *
     * This is the catch-all exception handler that:
     * - Catches any exception not handled by more specific handlers
     * - Returns HTTP 500 Internal Server Error with a generic message
     * - Logs the full exception stack trace for debugging
     * - Protects sensitive information by not exposing details to the client
     *
     * Behavior:
     * - Returns a generic error message to the client for security
     * - Logs the complete exception with stack trace at ERROR level
     * - Uses the exception class name as the error type
     * - Indicates an unexpected system error
     *
     * Example Response:
     * {
     *   "status": 500,
     *   "message": "An unexpected error occurred. Please try again later.",
     *   "errorType": "NullPointerException",
     *   "path": "/api/accounts/1"
     * }
     *
     * Note: The actual exception details are logged server-side and not exposed to the client.
     *
     * @param ex any exception not handled by more specific handlers
     * @param request the current web request context
     * @return ResponseEntity with generic ErrorResponse and HTTP 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);

        // Create a generic error response without exposing sensitive details
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred. Please try again later.",
                ex.getClass().getSimpleName(),
                getPath(request)
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Extract the request path from the WebRequest context.
     *
     * Purpose:
     * - Utility method to extract the URI from WebRequest for error logging
     * - Removes Spring's "uri=" prefix to get the clean path
     * - Used in all exception handlers to include the path in the error response
     *
     * Implementation:
     * - Calls request.getDescription(false) to get the request description
     * - Parses and removes the "uri=" prefix if present
     * - Returns the clean request path for the error response
     *
     * Example:
     * Input: "uri=/api/accounts/1"
     * Output: "/api/accounts/1"
     *
     * @param request the web request context
     * @return the clean request path without prefix
     */
    private String getPath(WebRequest request) {
        String path = request.getDescription(false);
        // Remove "uri=" prefix if present to get the clean path
        if (path.startsWith("uri=")) {
            path = path.substring(4);
        }
        return path;
    }
}

