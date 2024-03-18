package com.example.wallet.controller;

import com.example.wallet.apierror.Apierror;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;

@ControllerAdvice
@RestController
@Slf4j
public class Exceptionhandler {
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Apierror> exception(Exception exception) {
        Apierror apierror = new Apierror("Auth missing",400);
        return new ResponseEntity<>(apierror,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = NullPointerException.class)
    public ResponseEntity<Apierror> exception(NullPointerException exception) {
        Apierror apierror = new Apierror("Null pointer exception",406);
        return new ResponseEntity<>(apierror,HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(value = HttpServerErrorException.InternalServerError.class)
    public ResponseEntity<Apierror> exception(HttpServerErrorException.InternalServerError exception) {
        Apierror apierror = new Apierror("Error processing request",404);
        return new ResponseEntity<>(apierror,HttpStatus.NOT_FOUND);
    }
}
