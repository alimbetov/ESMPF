package com.esmpf.web;

import com.esmpf.shared.exception.EntityNotFoundException;
import com.esmpf.shared.security.SecurityAccessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    ProblemDetail notFound(EntityNotFoundException exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Resource not found", exception.getMessage(),
                "entity-not-found", request);
    }

    @ExceptionHandler(SecurityAccessException.class)
    ProblemDetail forbidden(SecurityAccessException exception, HttpServletRequest request) {
        return problem(HttpStatus.FORBIDDEN, "Access denied", exception.getMessage(),
                "access-denied", request);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail optimisticLock(
            OptimisticLockingFailureException exception,
            HttpServletRequest request
    ) {
        return problem(HttpStatus.CONFLICT, "Concurrent modification",
                "The resource was changed by another request",
                "optimistic-lock-conflict", request);
    }

    @ExceptionHandler(IllegalStateException.class)
    ProblemDetail invalidState(IllegalStateException exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Invalid resource state", exception.getMessage(),
                "invalid-resource-state", request);
    }

    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
    ProblemDetail badRequest(Exception exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", "Request parameters are invalid",
                "invalid-request", request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail malformedBody(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Malformed request body",
                "The request body cannot be parsed", "malformed-request-body", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, "Validation failed",
                "One or more request fields are invalid", "validation-failed", request);
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        detail.setProperty("errors", errors);
        return detail;
    }

    private static ProblemDetail problem(
            HttpStatus status,
            String title,
            String detail,
            String code,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail == null ? title : detail);
        problem.setTitle(title);
        problem.setType(URI.create("https://esmpf.dev/problems/" + code));
        problem.setProperty("code", code.toUpperCase().replace('-', '_'));
        problem.setProperty("path", request.getRequestURI());
        return problem;
    }
}
