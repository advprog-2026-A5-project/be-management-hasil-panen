package id.ac.ui.cs.advprog.hasilpanen.config;

import id.ac.ui.cs.advprog.hasilpanen.dto.ApiErrorResponse;
import id.ac.ui.cs.advprog.hasilpanen.dto.ApiErrorCode;
import id.ac.ui.cs.advprog.hasilpanen.service.DuplicateHarvestSubmissionException;
import id.ac.ui.cs.advprog.hasilpanen.service.AuthenticationRequiredException;
import id.ac.ui.cs.advprog.hasilpanen.service.HarvestNotFoundException;
import id.ac.ui.cs.advprog.hasilpanen.service.HarvestTerminalStatusException;
import id.ac.ui.cs.advprog.hasilpanen.service.MandorUnauthorizedAccessException;
import id.ac.ui.cs.advprog.hasilpanen.service.RoleForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, "Request body is invalid", request.getRequestURI());
    }

    @ExceptionHandler(MandorUnauthorizedAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(
            MandorUnauthorizedAccessException ex,
            HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(RoleForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleRoleForbidden(
            RoleForbiddenException ex,
            HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(AuthenticationRequiredException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthRequired(
            AuthenticationRequiredException ex,
            HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ApiErrorCode.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(HarvestNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            HarvestNotFoundException ex,
            HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResource(
            NoResourceFoundException ex,
            HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "Resource not found", request.getRequestURI());
    }

    @ExceptionHandler({DuplicateHarvestSubmissionException.class, HarvestTerminalStatusException.class})
    public ResponseEntity<ApiErrorResponse> handleConflict(
            RuntimeException ex,
            HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ApiErrorCode.CONFLICT, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataConflict(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ApiErrorCode.CONFLICT, "conflict with existing data", request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleUploadLimit(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ApiErrorCode.BAD_REQUEST, "uploaded file exceeds configured max size", request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleInternal(
            Exception ex,
            HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.INTERNAL_SERVER_ERROR, "Unexpected error", request.getRequestURI());
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, ApiErrorCode error, String message, String path) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(Instant.now(), status.value(), error, message, path));
    }
}
