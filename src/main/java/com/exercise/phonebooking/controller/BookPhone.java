package com.exercise.phonebooking.controller;

import java.time.LocalDateTime;

public record BookPhone(int phoneId, String bookedBy, LocalDateTime bookedAt) {
}
