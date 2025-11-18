package org.devconnect.devconnectbackend.exception;

public class ProjectAlreadyClaimedException extends RuntimeException {
    public ProjectAlreadyClaimedException(String message) {
        super(message);
    }
}
