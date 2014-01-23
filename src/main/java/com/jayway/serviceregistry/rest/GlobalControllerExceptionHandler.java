package com.jayway.serviceregistry.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice
class GlobalControllerExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<String> handleIllegalArguments(IllegalArgumentException e) {
        return new ResponseEntity<>("{\"reason\" : \"" + e.getMessage() + "\"}", BAD_REQUEST);
    }
}