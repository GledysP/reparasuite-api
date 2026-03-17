package com.reparasuite.api.exception;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiErrorResponse> handleApiException(
      ApiException ex,
      HttpServletRequest request
  ) {
    HttpStatus status = HttpStatus.valueOf(ex.getStatus());

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

  @ExceptionHandler({
      MethodArgumentNotValidException.class,
      BindException.class,
      IllegalArgumentException.class,
      HttpMessageNotReadableException.class,
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
    } else if (ex instanceof HttpMessageNotReadableException) {
      message = "El cuerpo de la solicitud es inválido o tiene un formato incorrecto";
    } else if (ex instanceof DateTimeParseException) {
      message = "Formato de fecha/hora inválido";
    } else {
      message = ex.getMessage() != null && !ex.getMessage().isBlank()
          ? ex.getMessage()
          : "Solicitud inválida";
    }

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
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
        new ApiErrorResponse(
            OffsetDateTime.now(),
            403,
            "Forbidden",
            ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage()
                : "No autorizado",
            request.getRequestURI()
        )
    );
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleUnexpected(
      Exception ex,
      HttpServletRequest request
  ) {
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