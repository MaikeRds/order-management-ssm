package br.com.maike.order_managment_ssm.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ResourceExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> notFound(NotFoundException e, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
