package com.example.wallet.Exception;

import com.example.wallet.apierror.Apierror;
import com.example.wallet.controller.ExceptionController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExceptionControllerTest {

    private ExceptionController exceptionController;

    @BeforeEach
    public void setUp() {
        exceptionController = new ExceptionController();
    }

    @Test
    public void testRuntimeExceptionHandler() {
        RuntimeException runtimeException = new RuntimeException("Runtime Exception");

        ResponseEntity<Apierror> responseEntity = exceptionController.exception(runtimeException);

        assertEquals(HttpStatus.GONE, responseEntity.getStatusCode());
        assertEquals("Request not processed", Objects.requireNonNull(responseEntity.getBody()).getErrorDescription());
        assertEquals(410, responseEntity.getBody().getResponseCode());
    }

    @Test
    public void testNullPointerExceptionHandler() {
        NullPointerException nullPointerException = new NullPointerException("Null Pointer Exception");

        ResponseEntity<Apierror> responseEntity = exceptionController.exception(nullPointerException);

        assertEquals(HttpStatus.NOT_ACCEPTABLE, responseEntity.getStatusCode());
        assertEquals("Null pointer exception", Objects.requireNonNull(responseEntity.getBody()).getErrorDescription());
        assertEquals(406, responseEntity.getBody().getResponseCode());
    }

    @Test
    public void testInternalServerErrorHandler() {
        HttpServerErrorException internalServerError =
                new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);

        ResponseEntity<Apierror> responseEntity = exceptionController.exception(internalServerError);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Error processing request", Objects.requireNonNull(responseEntity.getBody()).getErrorDescription());
        assertEquals(404, responseEntity.getBody().getResponseCode());
    }
}
