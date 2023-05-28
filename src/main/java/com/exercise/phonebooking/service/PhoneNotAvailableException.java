package com.exercise.phonebooking.service;

import java.time.Instant;

public class PhoneNotAvailableException extends RuntimeException {
    private final String model;
    private final String bookedBy;
    private final Instant bookedAt;

    public PhoneNotAvailableException(String model, String bookedBy, Instant bookedAt) {
        super(String.format("Fail to book [%s]. Phone was borrowed by [%s] on [%s] and not yet returned", model, bookedBy, bookedAt.toString()));

        this.model = model;
        this.bookedBy = bookedBy;
        this.bookedAt = bookedAt;
    }

    public String getModel() {
        return model;
    }

    public String getBookedBy() {
        return bookedBy;
    }

    public Instant getBookedAt() {
        return bookedAt;
    }
}
