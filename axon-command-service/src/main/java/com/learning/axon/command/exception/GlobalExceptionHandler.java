package com.learning.axon.command.exception;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

/**
 * GoF: Chain of Responsibility — intercepts exceptions propagating out of controllers
 * and maps them to RFC 9457 ProblemDetail responses.
 *
 * <p>Spring Boot 4 / Spring 7 natively returns ProblemDetail for standard Spring
 * exceptions. This handler extends that to Axon-specific exceptions.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problem.setTitle("Validation Failed");
        problem.setType(URI.create("https://axon.example.com/errors/validation"));
        return problem;
    }

    @ExceptionHandler(CommandExecutionException.class)
    public ProblemDetail handleCommandExecution(CommandExecutionException ex) {
        log.error("Command execution failed", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
        problem.setTitle("Command Execution Failed");
        problem.setType(URI.create("https://axon.example.com/errors/command-failure"));
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid Argument");
        problem.setType(URI.create("https://axon.example.com/errors/invalid-argument"));
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        log.error("Unhandled exception", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        problem.setTitle("Internal Server Error");
        problem.setType(URI.create("https://axon.example.com/errors/internal"));
        return problem;
    }
}
