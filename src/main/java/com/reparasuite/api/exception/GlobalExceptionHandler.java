package com.reparasuite.api.exception;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiErrorResponse> handleApiException(
      ApiException ex,
      HttpServletRequest request
  ) {
    HttpStatus status = HttpStatus.valueOf(ex.getStatus());

    log.warn(
        "API exception {} {} -> {} {}",
        request.getMethod(),
        request.getRequestURI(),
        status.value(),
        ex.getMessage()
    );

    return ResponseEntity.status(status).body(
        new ApiErrorResponse(
            OffsetDateTime.now(),
            status.value(),
            status.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        )
    );
  }

  @ExceptionHandler(TooManyRequestsException.class)
  public ResponseEntity<ApiErrorResponse> handleTooManyRequests(
      TooManyRequestsException ex,
      HttpServletRequest request
  ) {
    log.warn(
        "Rate limit {} {} -> 429 {}",
        request.getMethod(),
        request.getRequestURI(),
        ex.getMessage()
    );

    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
        .body(new ApiErrorResponse(
            OffsetDateTime.now(),
            429,
            "Too Many Requests",
            ex.getMessage(),
            request.getRequestURI()
        ));
  }

  @ExceptionHandler({
      MethodArgumentNotValidException.class,
      BindException.class,
      IllegalArgumentException.class,
      DateTimeParseException.class
  })
  public ResponseEntity<ApiErrorResponse> handleBadRequest(
      Exception ex,
      HttpServletRequest request
  ) {
    String message;

    if (ex instanceof MethodArgumentNotValidException manv) {
      message = manv.getBindingResult()
          .getFieldErrors()
          .stream()
          .map(err -> err.getField() + ": " + err.getDefaultMessage())
          .collect(Collectors.joining("; "));
      if (message.isBlank()) {
        message = "Solicitud inválida";
      }
    } else if (ex instanceof BindException be) {
      message = be.getBindingResult()
          .getFieldErrors()
          .stream()
          .map(err -> err.getField() + ": " + err.getDefaultMessage())
          .collect(Collectors.joining("; "));
      if (message.isBlank()) {
        message = "Solicitud inválida";
      }
    } else {
      message = ex.getMessage() != null && !ex.getMessage().isBlank()
          ? ex.getMessage()
          : "Solicitud inválida";
    }

    log.warn(
        "Bad request {} {} -> 400 {}",
        request.getMethod(),
        request.getRequestURI(),
        message
    );

    return ResponseEntity.badRequest().body(
        new ApiErrorResponse(
            OffsetDateTime.now(),
            400,
            "Bad Request",
            message,
            request.getRequestURI()
        )
    );
  }

  @ExceptionHandler({
      AccessDeniedException.class,
      AuthorizationDeniedException.class,
      ForbiddenException.class,
      SecurityException.class
  })
  public ResponseEntity<ApiErrorResponse> handleForbidden(
      Exception ex,
      HttpServletRequest request
  ) {
    String message = ex.getMessage() != null && !ex.getMessage().isBlank()
        ? ex.getMessage()
        : "No autorizado";

    log.warn(
        "Forbidden {} {} -> 403 {}",
        request.getMethod(),
        request.getRequestURI(),
        message
    );

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
        new ApiErrorResponse(
            OffsetDateTime.now(),
            403,
            "Forbidden",
            message,
            request.getRequestURI()
        )
    );
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleUnexpected(
      Exception ex,
      HttpServletRequest request
  ) {
    log.error(
        "Unexpected error {} {} -> 500",
        request.getMethod(),
        request.getRequestURI(),
        ex
    );

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
        new ApiErrorResponse(
            OffsetDateTime.now(),
            500,
            "Internal Server Error",
            "Ha ocurrido un error interno",
            request.getRequestURI()
        )
    );
  }
}