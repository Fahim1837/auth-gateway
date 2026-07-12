package com.fahim1837.auth_gateway.error;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class ApiErrorHandler extends ResponseEntityExceptionHandler{
    ApiResponse response;

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        response = new ApiResponse(status.value(), "Validation failed. Please check the submitted fields.");

        Map<String, String> errorObject = new HashMap<>();

        List<FieldError> fieldError = ex.getFieldErrors();
        Iterator<FieldError> elements = fieldError.iterator();
        
        while (elements.hasNext()) {
            FieldError element = elements.next();
            errorObject.put(element.getField(), element.getDefaultMessage());
        }
        response.setError(errorObject);

        var entity = new ResponseEntity<Object>(response, HttpStatus.valueOf(status.value()));
        return entity;
    }
}
