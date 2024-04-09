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
public class ExceptionController {
    @ExceptionHandler(value = RuntimeException.class )
    public ResponseEntity<Apierror> exception(Exception exception) {
        Apierror apierror = new Apierror("Request not processed",410);
        return new ResponseEntity<>(apierror,HttpStatus.GONE);
    }

    @ExceptionHandler(value = NullPointerException.class)
    public ResponseEntity<Apierror> exception(NullPointerException exception) {
        Apierror apierror = new Apierror("Null pointer exception",406);
        return new ResponseEntity<>(apierror,HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(value = HttpServerErrorException.class)
    public ResponseEntity<Apierror> exception(HttpServerErrorException exception) {
        Apierror apierror = new Apierror("Error processing request",404);
        return new ResponseEntity<>(apierror,HttpStatus.NOT_FOUND);
    }
}
