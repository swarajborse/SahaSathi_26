package com.sahasathi.exception;

public class AgeVerificationRequiredException extends RuntimeException {

    public AgeVerificationRequiredException() {
        super("Age verification required. Please verify your age to join this group.");
    }
}
