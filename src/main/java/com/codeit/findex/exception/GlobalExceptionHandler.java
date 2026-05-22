package com.codeit.findex.exception;

import com.codeit.findex.dto.ErrorResponse;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException exception) {
    HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        status.value(),
        exception.getReason(),
        status.getReasonPhrase()
    );

    return ResponseEntity.status(status).body(response);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
      HttpMessageNotReadableException exception
  ) {
    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        HttpStatus.BAD_REQUEST.value(),
        "잘못된 요청입니다.",
        exception.getMessage()
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
      IllegalArgumentException exception
  ) {
    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        HttpStatus.BAD_REQUEST.value(),
        exception.getMessage(),
        HttpStatus.BAD_REQUEST.getReasonPhrase()
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception exception) {
    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "서버 오류가 발생했습니다.",
        exception.getMessage()
    );

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

}
