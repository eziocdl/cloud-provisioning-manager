package com.eziocdl.domain.exception;

public class PolicyViolationException extends RuntimeException {

    private final String userRole;
    private final String violatedResource;
    private final String requestedValue;
    private final String maxAllowed;

    public PolicyViolationException(String userRole, String violatedResource, String requestedValue, String maxAllowed) {
        super(String.format(
                "Policy violation: User with role '%s' cannot request %s=%s. Maximum allowed: %s",
                userRole, violatedResource, requestedValue, maxAllowed
        ));
        this.userRole = userRole;
        this.violatedResource = violatedResource;
        this.requestedValue = requestedValue;
        this.maxAllowed = maxAllowed;
    }

    public String getUserRole() {
        return userRole;
    }

    public String getViolatedResource() {
        return violatedResource;
    }

    public String getRequestedValue() {
        return requestedValue;
    }

    public String getMaxAllowed() {
        return maxAllowed;
    }
}
