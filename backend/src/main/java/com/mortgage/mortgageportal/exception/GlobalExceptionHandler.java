package com.mortgage.mortgageportal.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex, HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("path", request.getRequestURI());
        body.put("errorCode", ex.getClass().getSimpleName());
        body.put("detail", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, 
                                                                 org.springframework.http.HttpHeaders headers, 
                                                                 org.springframework.http.HttpStatusCode status, 
                                                                 org.springframework.web.context.request.WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        body.put("errorCode", "ValidationError");
        body.put("detail", ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}