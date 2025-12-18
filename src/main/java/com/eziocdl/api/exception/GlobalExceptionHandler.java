package com.eziocdl.api.exception;

import com.eziocdl.domain.exception.PolicyViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PolicyViolationException.class)
    public ProblemDetail handlePolicyViolation(PolicyViolationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                ex.getMessage()
        );

        problem.setTitle("Policy Violation");
        problem.setType(URI.create("https://cpm.org/errors/policy-violation"));
        problem.setProperty("userRole", ex.getUserRole());
        problem.setProperty("violatedResource", ex.getViolatedResource());
        problem.setProperty("requestedValue", ex.getRequestedValue());
        problem.setProperty("maxAllowed", ex.getMaxAllowed());
        problem.setProperty("timestamp", Instant.now());

        System.err.println("🚫 [Policy] BLOCKED: " + ex.getMessage());

        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );

        problem.setTitle("Invalid Request");
        problem.setType(URI.create("https://cpm.org/errors/invalid-request"));
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }
}
